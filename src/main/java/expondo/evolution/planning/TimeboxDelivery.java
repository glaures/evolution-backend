package expondo.evolution.planning;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

/**
 * Tracks delivery progress on a specific deliverable during a timebox.
 * Used for EVO calculation.
 */
@Entity
@Table(name = "timebox_deliveries", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"timebox_report_id", "deliverable_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class TimeboxDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timebox_report_id", nullable = false)
    private TimeboxReport timeboxReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliverable_id", nullable = false)
    private Deliverable deliverable;

    /**
     * Delivery progress made THIS timebox (delta, not cumulative).
     * E.g., if deliverable was 30% done and is now 50% done, this is 20.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal progressPercentage;

    /**
     * Calculate EVO points earned for this delivery.
     * Requires the commitment's normalized value.
     */
    public BigDecimal calculateEvoPoints(BigDecimal normalizedValue) {
        return normalizedValue.multiply(progressPercentage).divide(BigDecimal.valueOf(100));
    }
}
