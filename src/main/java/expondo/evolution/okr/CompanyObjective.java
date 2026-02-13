package expondo.evolution.okr;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company_objectives")
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyObjective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private Cycle cycle;

    @OneToMany(mappedBy = "companyObjective", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KeyResult> keyResults = new ArrayList<>();

    @OneToMany(mappedBy = "companyObjective", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tactic> tactics = new ArrayList<>();
}