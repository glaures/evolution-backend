package expondo.evolution.planning;

import expondo.evolution.okr.Tactic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

/**
 * Tracks effort spent on a specific tactic during a timebox.
 * Used for BSI calculation and to see total investment per strategic item.
 */
@Entity
@Table(name = "timebox_efforts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"timebox_report_id", "tactic_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class TimeboxEffort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timebox_report_id", nullable = false)
    private TimeboxReport timeboxReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tactic_id", nullable = false)
    private Tactic tactic;

    /**
     * Percentage of total team effort spent on this tactic (0-100).
     * Sum of all TimeboxEfforts for a report should equal the BSI.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal effortPercentage;
}
