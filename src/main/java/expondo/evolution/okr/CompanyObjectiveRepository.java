package expondo.evolution.okr;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyObjectiveRepository extends JpaRepository<CompanyObjective, Long> {

    /**
     * Active COs for a cycle, ordered by code.
     */
    @Query("SELECT o FROM CompanyObjective o WHERE o.cycle.id = :cycleId " +
           "AND o.archived = false ORDER BY o.code ASC")
    List<CompanyObjective> findByCycleIdOrderByCodeAsc(@Param("cycleId") Long cycleId);

    Optional<CompanyObjective> findByJiraObjectiveName(String jiraObjectiveName);
}
