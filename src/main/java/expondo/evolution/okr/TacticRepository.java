package expondo.evolution.okr;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TacticRepository extends JpaRepository<Tactic, Long> {
    List<Tactic> findByCompanyObjectiveIdOrderByPriorityAsc(Long objectiveId);

    @Query("SELECT MAX(t.priority) FROM Tactic t WHERE t.companyObjective.id = :objectiveId")
    Integer findMaxPriorityByObjectiveId(@Param("objectiveId") Long objectiveId);

    @Query("SELECT t FROM Tactic t WHERE t.companyObjective.cycle.id = :cycleId ORDER BY t.priority ASC")
    List<Tactic> findByCycleIdOrderByPriorityAsc(@Param("cycleId") Long cycleId);
}