package expondo.evolution.planning;

import expondo.evolution.okr.Tactic;
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
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tactic_id", nullable = false)
    private Tactic tactic;

    @OneToMany(mappedBy = "deliverable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commitment> commitments = new ArrayList<>();

    @OneToMany(mappedBy = "deliverable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeboxEffort> timeboxEfforts = new ArrayList<>();

    @OneToMany(mappedBy = "deliverable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeboxDelivery> timeboxDeliveries = new ArrayList<>();

    public BigDecimal getWeightedValue() {
        int baseValue = tactic.getBaseValue();
        return weight.multiply(BigDecimal.valueOf(baseValue)).divide(BigDecimal.valueOf(100));
    }
}