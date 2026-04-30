package expondo.evolution.jira;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JiraSyncRunRepository extends JpaRepository<JiraSyncRun, Long> {
    List<JiraSyncRun> findAllByOrderByStartedAtDesc(Pageable pageable);
}
