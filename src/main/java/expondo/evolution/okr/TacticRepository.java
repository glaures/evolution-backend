package expondo.evolution.okr;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TacticRepository extends JpaRepository<Tactic, Long> {

    @Query("SELECT t FROM Tactic t WHERE t.companyObjective.id = :objectiveId " +
            "AND t.archived = false ORDER BY t.priority ASC")
    List<Tactic> findByCompanyObjectiveIdOrderByPriorityAsc(@Param("objectiveId") Long objectiveId);

    @Query("SELECT MAX(t.priority) FROM Tactic t WHERE t.companyObjective.id = :objectiveId " +
            "AND t.archived = false")
    Integer findMaxPriorityByObjectiveId(@Param("objectiveId") Long objectiveId);

    @Query("SELECT MAX(t.priority) FROM Tactic t WHERE t.companyObjective.cycle.id = :cycleId " +
            "AND t.archived = false")
    Integer findMaxPriorityByCycleId(@Param("cycleId") Long cycleId);

    @Query("SELECT t FROM Tactic t WHERE t.companyObjective.cycle.id = :cycleId " +
            "AND t.archived = false ORDER BY t.priority ASC")
    List<Tactic> findByCycleIdOrderByPriorityAsc(@Param("cycleId") Long cycleId);

    List<Tactic> findByArchivedFalse();

    @Query("SELECT COUNT(t) FROM Tactic t WHERE t.companyObjective.id = :objectiveId " +
            "AND t.archived = false")
    long countActiveByObjectiveId(@Param("objectiveId") Long objectiveId);

    long countByCompanyObjectiveId(Long companyObjectiveId);

    Optional<Tactic> findByJiraIssueKey(String jiraIssueKey);

    /**
     * Tactics whose priority needs to be pushed to JIRA:
     * - linked to JIRA (jiraIssueKey not null)
     * - active (not archived)
     * - have a priority
     * - the last pushed value differs from current (or has never been pushed)
     * - the priority hasn't changed in the last quiet period (debounce)
     */
    @Query("SELECT t FROM Tactic t WHERE t.jiraIssueKey IS NOT NULL " +
            "AND t.archived = false " +
            "AND t.priority IS NOT NULL " +
            "AND (t.jiraPriorityPushed IS NULL OR t.jiraPriorityPushed <> t.priority) " +
            "AND (t.priorityChangedAt IS NULL OR t.priorityChangedAt < :cutoff)")
    List<Tactic> findPushCandidates(@Param("cutoff") LocalDateTime cutoff);

    // Add this to TacticRepository (next to the existing findPushCandidates method).
// The two queries differ:
//   - findPushCandidates(cutoff): used by the scheduler. Filters by quiet period.
//   - findPushCandidatesByCycle(cycleId): used right after Save Order. Ignores
//     the quiet period because the user just explicitly committed.
    @Query("SELECT t FROM Tactic t " +
            "WHERE t.jiraIssueKey IS NOT NULL " +
            "  AND t.archived = false " +
            "  AND t.priority IS NOT NULL " +
            "  AND (t.jiraPriorityPushed IS NULL OR t.jiraPriorityPushed <> t.priority) " +
            "  AND t.companyObjective.cycle.id = :cycleId")
    List<Tactic> findPushCandidatesByCycle(@Param("cycleId") Long cycleId);
}
