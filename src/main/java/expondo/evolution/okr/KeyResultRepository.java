package expondo.evolution.okr;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface KeyResultRepository extends JpaRepository<KeyResult, Long> {

    List<KeyResult> findByCompanyObjectiveId(Long companyObjectiveId);

    /**
     * Find all Key Results belonging to a cycle (via CompanyObjective).
     */
    @Query("SELECT kr FROM KeyResult kr WHERE kr.companyObjective.cycle.id = :cycleId ORDER BY kr.companyObjective.code, kr.code")
    List<KeyResult> findByCycleId(Long cycleId);
}