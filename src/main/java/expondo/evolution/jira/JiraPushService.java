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
 * - Tactic.priorityChangedAt is older than the configured quiet period
 *   (debounce — gives the user time to keep dragging without spamming JIRA)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JiraPushService {

    private final TacticRepository tacticRepository;
    private final RestClient jiraRestClient;
    private final JiraProperties props;

    /**
     * Push all eligible tactics. Called by the scheduler.
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

        if (candidates.isEmpty()) return 0;

        int succeeded = 0;
        for (Tactic t : candidates) {
            try {
                pushOne(t.getId());
                succeeded++;
            } catch (Exception e) {
                log.warn("Failed to push priority for tactic {} (JIRA key {}): {}",
                        t.getCode(), t.getJiraIssueKey(), e.getMessage());
            }
        }

        if (succeeded > 0) {
            log.info("Pushed priorities for {}/{} tactic(s) to JIRA", succeeded, candidates.size());
        }
        return succeeded;
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
                    .uri("/rest/api/3/issue/{key}", t.getJiraIssueKey())
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
}
