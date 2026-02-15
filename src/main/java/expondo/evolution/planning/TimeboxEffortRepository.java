package expondo.evolution.planning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeboxEffortRepository extends JpaRepository<TimeboxEffort, Long> {

    @Query("""
        SELECT e FROM TimeboxEffort e
        JOIN FETCH e.timeboxReport r
        JOIN FETCH r.team
        JOIN FETCH r.timebox
        WHERE e.tactic.id = :tacticId
        ORDER BY r.timebox.number ASC, r.team.name ASC
    """)
    List<TimeboxEffort> findByTacticIdWithDetails(Long tacticId);

    /**
     * For each tactic in a cycle, returns the number of distinct timeboxes where effort was reported.
     * Result: Object[] { tacticId (Long), timeboxCount (Long) }
     */
    @Query("""
        SELECT e.tactic.id, COUNT(DISTINCT r.timebox.id)
        FROM TimeboxEffort e
        JOIN e.timeboxReport r
        WHERE r.timebox.cycle.id = :cycleId
        GROUP BY e.tactic.id
    """)
    List<Object[]> findEffortCoverageByCycleId(Long cycleId);
}