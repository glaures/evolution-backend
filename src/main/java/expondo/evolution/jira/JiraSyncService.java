package expondo.evolution.jira;

import expondo.evolution.okr.CompanyObjective;
import expondo.evolution.okr.CompanyObjectiveRepository;
import expondo.evolution.okr.Cycle;
import expondo.evolution.okr.CycleRepository;
import expondo.evolution.okr.Tactic;
import expondo.evolution.okr.TacticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraSyncService {

    private final JiraSyncFetcher fetcher;
    private final TacticRepository tacticRepository;
    private final CompanyObjectiveRepository objectiveRepository;
    private final CycleRepository cycleRepository;
    private final JiraSyncRunRepository runRepository;

    @Transactional
    public JiraSyncRun runSync() {
        JiraSyncRun run = new JiraSyncRun();
        run.setStartedAt(LocalDateTime.now());
        run.setStatus("RUNNING");
        run = runRepository.save(run);

        try {
            Cycle activeCycle = cycleRepository.findByCurrent(true)
                    .orElseThrow(() -> new IllegalStateException(
                            "No active cycle (Cycle.current = true) found. " +
                                    "Mark a cycle as active before running the sync."));

            Map<String, CompanyObjective> coByJiraName = new HashMap<>();
            for (CompanyObjective co : objectiveRepository.findByCycleIdOrderByCodeAsc(activeCycle.getId())) {
                if (co.getJiraObjectiveName() != null && !co.getJiraObjectiveName().isBlank()) {
                    coByJiraName.put(co.getJiraObjectiveName(), co);
                }
            }
            if (coByJiraName.isEmpty()) {
                throw new IllegalStateException(
                        "No CompanyObjectives with jira_objective_name found in active cycle. " +
                                "Create COs in Evolution first and set their jiraObjectiveName.");
            }
            log.info("Sync configured against {} objective(s): {}",
                    coByJiraName.size(), coByJiraName.keySet());

            List<JiraIssueDto> jiraIssues = fetcher.fetchAllTactics();
            run.setIssuesFetched(jiraIssues.size());

            boolean isFirstSync = tacticRepository.findAll().stream()
                    .noneMatch(t -> t.getJiraIssueKey() != null);

            Map<String, Tactic> existingByKey = new HashMap<>();
            for (Tactic t : tacticRepository.findAll()) {
                if (t.getJiraIssueKey() != null) {
                    existingByKey.put(t.getJiraIssueKey(), t);
                }
            }

            Set<String> jiraKeysSeenInScope = new HashSet<>();
            Set<String> jiraKeysSeenAtAll = new HashSet<>();
            LocalDateTime now = LocalDateTime.now();

            int firstSyncPriorityCounter = 1;

            for (JiraIssueDto issue : jiraIssues) {
                jiraKeysSeenAtAll.add(issue.key());

                if (issue.objectiveName() == null || issue.objectiveName().isBlank()) {
                    run.setIssuesSkippedNoObjective(run.getIssuesSkippedNoObjective() + 1);
                    continue;
                }

                CompanyObjective co = coByJiraName.get(issue.objectiveName());
                if (co == null) {
                    run.setIssuesSkippedUnknownObjective(run.getIssuesSkippedUnknownObjective() + 1);
                    continue;
                }

                jiraKeysSeenInScope.add(issue.key());

                Tactic existing = existingByKey.get(issue.key());

                if (existing == null) {
                    Tactic created = createTactic(issue, co, isFirstSync, firstSyncPriorityCounter, now);
                    if (isFirstSync) firstSyncPriorityCounter++;
                    log.debug("Created tactic {} from JIRA issue {}", created.getCode(), issue.key());
                    run.setTacticsCreated(run.getTacticsCreated() + 1);
                } else if (existing.isArchived()) {
                    reactivateTactic(existing, issue, co, now);
                    log.debug("Reactivated tactic {} from JIRA issue {}", existing.getCode(), issue.key());
                    run.setTacticsReactivated(run.getTacticsReactivated() + 1);
                } else {
                    if (updateTactic(existing, issue, co, now)) {
                        run.setTacticsUpdated(run.getTacticsUpdated() + 1);
                    }
                }
            }

            for (Tactic t : existingByKey.values()) {
                if (t.isArchived()) continue;
                String key = t.getJiraIssueKey();

                if (!jiraKeysSeenAtAll.contains(key)) {
                    t.setArchived(true);
                    t.setLastSyncedAt(now);
                    tacticRepository.save(t);
                    run.setTacticsArchived(run.getTacticsArchived() + 1);
                } else if (!jiraKeysSeenInScope.contains(key)) {
                    t.setArchived(true);
                    t.setLastSyncedAt(now);
                    tacticRepository.save(t);
                    run.setTacticsArchivedObjectiveMoved(run.getTacticsArchivedObjectiveMoved() + 1);
                }
            }

            run.setStatus("SUCCESS");
            run.setFinishedAt(LocalDateTime.now());
            log.info("JIRA sync finished: fetched={}, created={}, updated={}, reactivated={}, " +
                            "archived={}, archivedObjectiveMoved={}, skippedNoObjective={}, skippedUnknownObjective={}",
                    run.getIssuesFetched(), run.getTacticsCreated(), run.getTacticsUpdated(),
                    run.getTacticsReactivated(), run.getTacticsArchived(),
                    run.getTacticsArchivedObjectiveMoved(),
                    run.getIssuesSkippedNoObjective(), run.getIssuesSkippedUnknownObjective());
            return runRepository.save(run);

        } catch (Exception e) {
            log.error("JIRA sync failed", e);
            run.setStatus("FAILED");
            run.setMessage(e.getMessage());
            run.setFinishedAt(LocalDateTime.now());
            return runRepository.save(run);
        }
    }

    private Tactic createTactic(JiraIssueDto issue, CompanyObjective co,
                                boolean isFirstSync, int firstSyncPriority, LocalDateTime now) {
        Tactic t = new Tactic();
        t.setCompanyObjective(co);
        t.setTitle(safe(issue.summary()));
        t.setDescription(issue.description());
        t.setJiraIssueKey(issue.key());
        t.setJiraDepartments(issue.departments());
        t.setLastSyncedAt(now);

        long count = tacticRepository.findByCompanyObjectiveIdOrderByPriorityAsc(co.getId()).size();
        t.setCode(co.getCode() + ".T" + (count + 1));

        int priority;
        if (isFirstSync) {
            priority = firstSyncPriority;
        } else {
            Long cycleId = co.getCycle().getId();
            Integer max = tacticRepository.findMaxPriorityByCycleId(cycleId);
            priority = (max == null ? 0 : max) + 1;
        }
        t.setPriority(priority);
        t.setPriorityChangedAt(now);

        return tacticRepository.save(t);
    }

    private void reactivateTactic(Tactic t, JiraIssueDto issue, CompanyObjective co, LocalDateTime now) {
        t.setArchived(false);
        t.setTitle(safe(issue.summary()));
        t.setDescription(issue.description());
        t.setJiraDepartments(issue.departments());
        t.setCompanyObjective(co);
        t.setLastSyncedAt(now);

        Long cycleId = co.getCycle().getId();
        Integer max = tacticRepository.findMaxPriorityByCycleId(cycleId);
        int priority = (max == null ? 0 : max) + 1;
        t.setPriority(priority);
        t.setPriorityChangedAt(now);

        tacticRepository.save(t);
    }

    private boolean updateTactic(Tactic t, JiraIssueDto issue, CompanyObjective co, LocalDateTime now) {
        boolean changed = false;
        String newTitle = safe(issue.summary());
        if (!Objects.equals(t.getTitle(), newTitle)) {
            t.setTitle(newTitle);
            changed = true;
        }
        if (!Objects.equals(t.getDescription(), issue.description())) {
            t.setDescription(issue.description());
            changed = true;
        }
        if (!Objects.equals(t.getJiraDepartments(), issue.departments())) {
            t.setJiraDepartments(issue.departments());
            changed = true;
        }
        if (t.getCompanyObjective() == null || !t.getCompanyObjective().getId().equals(co.getId())) {
            t.setCompanyObjective(co);
            changed = true;
        }
        t.setLastSyncedAt(now);
        if (changed) {
            tacticRepository.save(t);
        }
        return changed;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}