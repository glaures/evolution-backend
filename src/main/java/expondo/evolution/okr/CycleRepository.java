package expondo.evolution.okr;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CycleRepository extends JpaRepository<Cycle, Long> {
    Optional<Cycle> findByCurrent(boolean current);
}