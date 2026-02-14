package expondo.evolution.okr;

import expondo.evolution.user.Unit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "tactics")
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tactic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Priority rank for EVO scoring.
     * Rank 1-4: 10 points, 5: 7, 6: 6, 7: 5, 8: 4, 9: 3, 10: 2, 11+: 1
     */
    @Column
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_objective_id", nullable = false)
    private CompanyObjective companyObjective;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_unit_id")
    private Unit responsibleUnit;

    /**
     * EVO score based on priority rank.
     * Rank 1-4: 10 points, 5-9: 7 points, 10+: 5 points
     */
    public int getScore() {
        if (priority == null) return 0;
        if (priority <= 4) return 10;
        if (priority <= 9) return 7;
        return 5;
    }
}