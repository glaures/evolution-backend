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

    @Column
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_objective_id", nullable = false)
    private CompanyObjective companyObjective;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_unit_id")
    private Unit responsibleUnit;

    @Column(unique = true)
    private String jiraIssueKey;

    /**
     * Departments value pulled from the JIRA issue, stored verbatim as a comma-separated
     * string (e.g. "Finance, Operations"). Read-only from Evolution's perspective —
     * fully owned by the JIRA sync. Will be replaced by a proper Unit mapping later.
     */
    @Column(length = 500)
    private String jiraDepartments;

    @Column(nullable = false)
    private boolean archived = false;

    private LocalDateTime lastSyncedAt;

    private Integer jiraPriorityPushed;

    private LocalDateTime priorityChangedAt;

    public int getScore() {
        if (priority == null) return 0;
        if (priority <= 4) return 10;
        if (priority <= 9) return 7;
        return 5;
    }
}