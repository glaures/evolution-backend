package expondo.evolution.okr;

import expondo.evolution.user.Unit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

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
     * Rank 1-4: 10 points, 5-9: 7 points, 10+: 5 points.
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
     * The JIRA issue key (e.g. "EXP-1175") this tactic is mirrored from.
     * Null if the tactic was created manually in Evolution.
     * Unique among non-null values.
     */
    @Column(unique = true)
    private String jiraIssueKey;

    /**
     * Soft-delete flag. Archived tactics:
     * - Are hidden from active UI lists
     * - Are not snapshotted into new timeboxes
     * - Remain referenceable from existing snapshots, deliveries and efforts
     *   so historical reports stay intact.
     */
    @Column(nullable = false)
    private boolean archived = false;

    /**
     * Last time the JIRA sync touched this tactic. Null for tactics that have
     * never been synced (manual or not yet linked).
     */
    private LocalDateTime lastSyncedAt;

    /**
     * EVO score based on priority rank.
     */
    public int getScore() {
        if (priority == null) return 0;
        if (priority <= 4) return 10;
        if (priority <= 9) return 7;
        return 5;
    }
}
