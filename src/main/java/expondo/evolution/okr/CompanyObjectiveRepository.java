package expondo.evolution.okr;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyObjectiveRepository extends JpaRepository<CompanyObjective, Long> {
    List<CompanyObjective> findByCycleIdOrderByCodeAsc(Long cycleId);
}