package expondo.evolution.jira;

import expondo.evolution.okr.Tactic;
import expondo.evolution.okr.TacticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Pushes Evolution priorities back to JIRA into the "Company Prio" custom field.
 *
 * Pushed only when:
 * - Tactic has a jiraIssueKey
 * - Tactic is not archived
 * - Tactic.priority != Tactic.jiraPriorityPushed (= the value last successfully pushed)
 *
 * Two trigger paths:
 *  1) Scheduler-driven (debounced): pushPendingPriorities() runs every 30s and only
 *     touches tactics whose priorityChangedAt is older than the configured quiet
 *     period. This is the safety net for failed direct pushes and also handles
 *     priority changes made outside the prioritization page.
 *  2) Direct-after-save: pushAllNowForCycle() runs synchronously right after the
 *     user clicks "Save Order" and ignores the quiet period — the user has just
 *     deliberately committed an order, so there's nothing to debounce.
 *
 * All JIRA edits are sent with notifyUsers=false to avoid spamming watchers
 * when many tickets are updated at once. This requires the service-account user
 * to have project-admin (or higher) permission on the project.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JiraPushService {

    private final TacticRepository tacticRepository;
    private final RestClient jiraRestClient;
    private final JiraProperties props;

    /**
     * Push all eligible tactics that are past the quiet period. Called by the scheduler.
     *
     * Each push runs in its own transaction (per tactic) so a single JIRA error
     * doesn't roll back the whole batch.
     *
     * @return number of tactics for which the push succeeded
     */
    public int pushPendingPriorities() {
        if (!props.isConfigured() || props.sync() == null
                || props.sync().fieldCompanyPrio() == null) {
            return 0;
        }

        int quietSeconds = props.sync().pushQuietPeriodSecondsOrDefault();
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(quietSeconds);

        List<Tactic> candidates = tacticRepository.findPushCandidates(cutoff);

        return pushBatch(candidates).succeeded();
    }

    /**
     * Push every tactic in the given cycle whose priority differs from
     * jiraPriorityPushed, ignoring the quiet period. Used right after Save Order.
     */
    public PushResult pushAllNowForCycle(Long cycleId) {
        if (!props.isConfigured() || props.sync() == null
                || props.sync().fieldCompanyPrio() == null) {
            return new PushResult(0, 0);
        }

        List<Tactic> candidates = tacticRepository.findPushCandidatesByCycle(cycleId);
        return pushBatch(candidates);
    }

    private PushResult pushBatch(List<Tactic> candidates) {
        if (candidates.isEmpty()) return new PushResult(0, 0);

        int succeeded = 0;
        int failed = 0;
        for (Tactic t : candidates) {
            try {
                pushOne(t.getId());
                succeeded++;
            } catch (Exception e) {
                failed++;
                log.warn("Failed to push priority for tactic {} (JIRA key {}): {}",
                        t.getCode(), t.getJiraIssueKey(), e.getMessage());
            }
        }

        if (succeeded > 0 || failed > 0) {
            log.info("JIRA push: {} succeeded, {} failed (out of {} candidates)",
                    succeeded, failed, candidates.size());
        }
        return new PushResult(succeeded, failed);
    }

    @Transactional
    public void pushOne(Long tacticId) {
        Tactic t = tacticRepository.findById(tacticId)
                .orElseThrow(() -> new IllegalStateException("Tactic vanished: " + tacticId));

        if (t.getJiraIssueKey() == null || t.isArchived() || t.getPriority() == null) {
            return;
        }

        String fieldId = props.sync().fieldCompanyPrio();
        Map<String, Object> body = Map.of(
                "fields", Map.of(fieldId, t.getPriority())
        );

        try {
            jiraRestClient.put()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/api/3/issue/{key}")
                            .queryParam("notifyUsers", "false")
                            .build(t.getJiraIssueKey()))
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            t.setJiraPriorityPushed(t.getPriority());
            tacticRepository.save(t);
        } catch (RestClientResponseException e) {
            throw new RuntimeException(
                    "JIRA returned " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        }
    }

    public record PushResult(int succeeded, int failed) {}
}