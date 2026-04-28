package expondo.evolution.okr;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company_objectives")
@Audited
@Getter
@Setter
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

    /**
     * The objective name as it appears in the JIRA "Objective" field of tactics.
     * COs created via JIRA sync are matched against this. Null if created manually.
     * Unique among non-null values.
     */
    @Column(unique = true)
    private String jiraObjectiveName;

    /**
     * Soft-delete flag. A CO with active tactics cannot be archived.
     */
    @Column(nullable = false)
    private boolean archived = false;

    /**
     * Last time the JIRA sync touched this CO.
     */
    private LocalDateTime lastSyncedAt;
}
