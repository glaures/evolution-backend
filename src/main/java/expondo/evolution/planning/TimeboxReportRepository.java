package expondo.evolution.planning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeboxReportRepository extends JpaRepository<TimeboxReport, Long> {

    Optional<TimeboxReport> findByTeamIdAndTimeboxId(Long teamId, Long timeboxId);

    List<TimeboxReport> findByTimeboxId(Long timeboxId);

    List<TimeboxReport> findByTeamId(Long teamId);
}
