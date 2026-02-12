package expondo.evolution.planning;

import expondo.evolution.okr.Cycle;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Entity
@Table(name = "commitments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "cycle_id", "deliverable_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Commitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private Cycle cycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliverable_id", nullable = false)
    private Deliverable deliverable;

    /**
     * Percentage of the deliverable this team commits to (0-100).
     * Usually 100, but allows for partial commitments if a deliverable
     * is shared across teams.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commitmentPercentage = BigDecimal.valueOf(100);

    /**
     * Calculated normalized value for this commitment within the team's
     * total commitments for the cycle. Set during normalization.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal normalizedValue;
}
