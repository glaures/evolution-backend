package expondo.evolution.planning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeboxTacticSnapshotRepository extends JpaRepository<TimeboxTacticSnapshot, Long> {

    Optional<TimeboxTacticSnapshot> findByTimeboxIdAndTacticId(Long timeboxId, Long tacticId);

    List<TimeboxTacticSnapshot> findByTimeboxId(Long timeboxId);

    boolean existsByTimeboxId(Long timeboxId);
}
