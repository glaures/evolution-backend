package expondo.evolution.planning;

import expondo.evolution.okr.KeyResult;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deliverables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Deliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code; // e.g., "D1.1.1"

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Weight as percentage of the Key Result (0-100).
     * All deliverables of a Key Result should sum to 100.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_result_id", nullable = false)
    private KeyResult keyResult;

    @OneToMany(mappedBy = "deliverable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commitment> commitments = new ArrayList<>();

    @OneToMany(mappedBy = "deliverable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeboxEffort> timeboxEfforts = new ArrayList<>();

    @OneToMany(mappedBy = "deliverable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeboxDelivery> timeboxDeliveries = new ArrayList<>();

    /**
     * Calculate the weighted value of this deliverable.
     * = KeyResult.baseValue * (weight / 100)
     */
    public BigDecimal getWeightedValue() {
        int baseValue = keyResult.getBaseValue();
        return weight.multiply(BigDecimal.valueOf(baseValue)).divide(BigDecimal.valueOf(100));
    }
}
