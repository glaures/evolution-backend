package expondo.evolution.okr;

import expondo.evolution.planning.DeliveryKeyResultImpact;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "key_results")
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KeyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Optional notes for qualitative context (e.g., Cycle review observations,
     * blockers, or reasons for deprioritization).
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_objective_id", nullable = false)
    private CompanyObjective companyObjective;

    /**
     * All delivery impacts referencing this Key Result (read-only navigation).
     * Used to determine whether this KR has been impacted or achieved by releases.
     */
    @OneToMany(mappedBy = "keyResult")
    private List<DeliveryKeyResultImpact> deliveryImpacts = new ArrayList<>();

    /**
     * Whether this Key Result has been fully achieved by at least one release.
     */
    public boolean isAchieved() {
        return deliveryImpacts.stream()
                .anyMatch(impact -> impact.getImpactType() == DeliveryKeyResultImpact.ImpactType.ACHIEVES);
    }

    /**
     * Whether this Key Result has been impacted (but not necessarily achieved) by any release.
     */
    public boolean isImpacted() {
        return !deliveryImpacts.isEmpty();
    }
}
