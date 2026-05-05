package expondo.evolution.planning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TimeboxTacticSnapshotRepository extends JpaRepository<TimeboxTacticSnapshot, Long> {

    Optional<TimeboxTacticSnapshot> findByTimeboxIdAndTacticId(Long timeboxId, Long tacticId);

    List<TimeboxTacticSnapshot> findByTimeboxId(Long timeboxId);

    boolean existsByTimeboxId(Long timeboxId);

    /**
     * Used by Tactic delete to gate hard deletion when historical snapshots
     * still reference the tactic.
     */
    boolean existsByTacticId(Long tacticId);

    /**
     * Loads snapshots for a timebox, eagerly fetching tactic + companyObjective
     * so callers don't trip over LazyInitialization when serializing.
     * Sorted by snapshot priority ascending (most important first).
     */
    @Query("SELECT s FROM TimeboxTacticSnapshot s " +
            "JOIN FETCH s.tactic t " +
            "JOIN FETCH t.companyObjective " +
            "WHERE s.timebox.id = :timeboxId " +
            "ORDER BY s.priorityAtOpen ASC")
    List<TimeboxTacticSnapshot> findByTimeboxIdWithTactic(@Param("timeboxId") Long timeboxId);

    /**
     * Distinct timebox IDs that already have at least one snapshot,
     * scoped to the given cycle. Order is applied in the service layer.
     */
    @Query("SELECT DISTINCT s.timebox.id FROM TimeboxTacticSnapshot s " +
            "WHERE s.timebox.cycle.id = :cycleId")
    List<Long> findTimeboxIdsWithSnapshotsByCycleId(@Param("cycleId") Long cycleId);
}