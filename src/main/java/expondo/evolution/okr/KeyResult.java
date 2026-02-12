package expondo.evolution.okr;

import expondo.evolution.planning.Deliverable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "key_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class KeyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code; // e.g., "KR1.1"

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Priority rank (1-4 = highest priority, then decreasing value)
     */
    @Column(nullable = false)
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_objective_id", nullable = false)
    private CompanyObjective companyObjective;

    @OneToMany(mappedBy = "keyResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deliverable> deliverables = new ArrayList<>();

    /**
     * Calculate base value from rank.
     * Rank 1-4: 1000 points each
     * Rank 5: 700
     * Rank 6: 600
     * Rank 7: 500
     * ... decreasing by 100, minimum 100
     */
    public int getBaseValue() {
        if (priority <= 4) {
            return 1000;
        }
        int value = 700 - ((priority - 5) * 100);
        return Math.max(value, 100);
    }
}
