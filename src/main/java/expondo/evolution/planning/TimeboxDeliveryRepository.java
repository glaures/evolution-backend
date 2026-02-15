package expondo.evolution.planning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeboxDeliveryRepository extends JpaRepository<TimeboxDelivery, Long> {

    @Query("""
        SELECT d FROM TimeboxDelivery d
        JOIN FETCH d.timeboxReport r
        JOIN FETCH r.team
        JOIN FETCH r.timebox
        LEFT JOIN FETCH d.keyResultImpacts i
        LEFT JOIN FETCH i.keyResult
        WHERE d.tactic.id = :tacticId
        ORDER BY r.timebox.number ASC
    """)
    List<TimeboxDelivery> findByTacticIdWithDetails(Long tacticId);
}