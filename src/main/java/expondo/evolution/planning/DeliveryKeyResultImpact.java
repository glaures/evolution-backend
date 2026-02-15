package expondo.evolution.planning;

import expondo.evolution.okr.KeyResult;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * Tracks the relationship between a release (TimeboxDelivery) and the Key Results
 * it influences. A release can either contribute toward a Key Result (IMPACTS)
 * or fully achieve it (ACHIEVES).
 */
@Entity
@Table(name = "delivery_key_result_impacts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"timebox_delivery_id", "key_result_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class DeliveryKeyResultImpact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timebox_delivery_id", nullable = false)
    private TimeboxDelivery timeboxDelivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_result_id", nullable = false)
    private KeyResult keyResult;

    /**
     * Type of impact this release has on the Key Result.
     * IMPACTS: contributes toward achieving the Key Result
     * ACHIEVES: fully achieves the Key Result
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImpactType impactType = ImpactType.IMPACTS;

    public enum ImpactType {
        /** Release contributes toward the Key Result */
        IMPACTS,
        /** Release fully achieves the Key Result */
        ACHIEVES
    }
}
