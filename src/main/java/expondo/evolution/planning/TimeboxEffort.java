package expondo.evolution.planning;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Tracks effort spent on a specific deliverable during a timebox.
 * Used for BSI calculation and to see total investment per strategic item.
 */
@Entity
@Table(name = "timebox_efforts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"timebox_report_id", "deliverable_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeboxEffort {

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
     * Percentage of total team effort spent on this deliverable (0-100).
     * Sum of all TimeboxEfforts for a report should equal the BSI.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal effortPercentage;
}
