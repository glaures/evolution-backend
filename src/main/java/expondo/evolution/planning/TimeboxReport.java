package expondo.evolution.planning;

import expondo.evolution.user.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timebox_reports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "timebox_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class TimeboxReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timebox_id", nullable = false)
    private Timebox timebox;

    /**
     * Percentage of effort spent on maintenance (0-100).
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal effortMaintenance = BigDecimal.ZERO;

    /**
     * Percentage of effort spent on administration (0-100).
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal effortAdministration = BigDecimal.ZERO;

    /**
     * Detailed breakdown of solution development effort per tactic.
     */
    @OneToMany(mappedBy = "timeboxReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeboxEffort> efforts = new ArrayList<>();

    /**
     * Releases delivered during this timebox.
     */
    @OneToMany(mappedBy = "timeboxReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeboxDelivery> deliveries = new ArrayList<>();

    /**
     * Calculate BSI (Business Strategy Investment) for this timebox.
     * BSI = Total Solution Development percentage = 100 - maintenance - administration
     */
    public BigDecimal getBsi() {
        return BigDecimal.valueOf(100)
                .subtract(effortMaintenance)
                .subtract(effortAdministration);
    }

    /**
     * Calculate total solution development effort from individual efforts.
     * Should equal getBsi() if data is consistent.
     */
    public BigDecimal getTotalSolutionDevelopmentEffort() {
        return efforts.stream()
                .map(TimeboxEffort::getEffortPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
