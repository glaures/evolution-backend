package expondo.evolution.okr;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeyResultRepository extends JpaRepository<KeyResult, Long> {
    List<KeyResult> findByCompanyObjectiveIdOrderByCodeAsc(Long objectiveId);
}