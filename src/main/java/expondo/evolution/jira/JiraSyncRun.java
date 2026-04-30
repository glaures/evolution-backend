package expondo.evolution.jira;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "jira_sync_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JiraSyncRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @Column(nullable = false, length = 32)
    private String status; // RUNNING | SUCCESS | FAILED

    @Column(nullable = false)
    private int issuesFetched = 0;

    @Column(nullable = false)
    private int tacticsCreated = 0;

    @Column(nullable = false)
    private int tacticsUpdated = 0;

    @Column(nullable = false)
    private int tacticsReactivated = 0;

    /**
     * Tactics archived because their JIRA issue is no longer found at all.
     */
    @Column(nullable = false)
    private int tacticsArchived = 0;

    /**
     * Tactics archived because their JIRA issue moved to an objective
     * that is not configured in the active cycle.
     */
    @Column(nullable = false)
    private int tacticsArchivedObjectiveMoved = 0;

    /**
     * JIRA issues skipped because they have no objective set at all.
     */
    @Column(nullable = false)
    private int issuesSkippedNoObjective = 0;

    /**
     * JIRA issues skipped because their objective is not configured as a
     * CompanyObjective with matching jira_objective_name in the active cycle.
     */
    @Column(nullable = false)
    private int issuesSkippedUnknownObjective = 0;

    @Column(columnDefinition = "TEXT")
    private String message;
}