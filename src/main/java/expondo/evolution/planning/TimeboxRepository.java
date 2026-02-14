package expondo.evolution.planning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeboxRepository extends JpaRepository<Timebox, Long> {

    List<Timebox> findByCycleIdOrderByNumberAsc(Long cycleId);

    void deleteAllByCycleId(Long cycleId);
}
