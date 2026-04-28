package expondo.evolution.okr;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TacticRepository extends JpaRepository<Tactic, Long> {

    /**
     * Active tactics for an objective, ordered by priority.
     * Archived tactics are excluded — they remain accessible by id but never
     * appear in active lists.
     */
    @Query("SELECT t FROM Tactic t WHERE t.companyObjective.id = :objectiveId " +
           "AND t.archived = false ORDER BY t.priority ASC")
    List<Tactic> findByCompanyObjectiveIdOrderByPriorityAsc(@Param("objectiveId") Long objectiveId);

    /**
     * Highest priority among active tactics in an objective.
     */
    @Query("SELECT MAX(t.priority) FROM Tactic t WHERE t.companyObjective.id = :objectiveId " +
           "AND t.archived = false")
    Integer findMaxPriorityByObjectiveId(@Param("objectiveId") Long objectiveId);

    /**
     * Highest priority among active tactics in a cycle.
     */
    @Query("SELECT MAX(t.priority) FROM Tactic t WHERE t.companyObjective.cycle.id = :cycleId " +
           "AND t.archived = false")
    Integer findMaxPriorityByCycleId(@Param("cycleId") Long cycleId);

    /**
     * Active tactics in a cycle, ordered by priority.
     */
    @Query("SELECT t FROM Tactic t WHERE t.companyObjective.cycle.id = :cycleId " +
           "AND t.archived = false ORDER BY t.priority ASC")
    List<Tactic> findByCycleIdOrderByPriorityAsc(@Param("cycleId") Long cycleId);

    /**
     * All tactics (including archived). Used by the snapshot service to also
     * snapshot archived tactics? — no: see findByArchivedFalse().
     */
    List<Tactic> findByArchivedFalse();

    /**
     * Active tactic count for an objective. Used to gate CO archival/deletion.
     */
    @Query("SELECT COUNT(t) FROM Tactic t WHERE t.companyObjective.id = :objectiveId " +
           "AND t.archived = false")
    long countActiveByObjectiveId(@Param("objectiveId") Long objectiveId);

    /**
     * Total tactic count for an objective (active + archived). Used to gate CO deletion.
     */
    long countByCompanyObjectiveId(Long companyObjectiveId);

    Optional<Tactic> findByJiraIssueKey(String jiraIssueKey);
}
