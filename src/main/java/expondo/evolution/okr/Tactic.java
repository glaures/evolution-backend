package expondo.evolution.okr;

import expondo.evolution.planning.Deliverable;
import expondo.evolution.user.Unit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tactics")
@Audited
@Data
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
     * Rank 1-4: 1000 points, Rank 5: 700, Rank 6: 600, etc.
     */
    @Column
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_objective_id", nullable = false)
    private CompanyObjective companyObjective;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_unit_id")
    private Unit responsibleUnit;

    @OneToMany(mappedBy = "tactic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deliverable> deliverables = new ArrayList<>();

    /**
     * Calculate base value from priority rank.
     * Rank 1-4: 1000 points each
     * Rank 5: 700
     * Rank 6: 600
     * Rank 7: 500
     * ... decreasing by 100, minimum 100
     */
    public int getBaseValue() {
        if (priority == null) return 0;
        if (priority <= 4) return 1000;
        int value = 700 - ((priority - 5) * 100);
        return Math.max(value, 100);
    }
}