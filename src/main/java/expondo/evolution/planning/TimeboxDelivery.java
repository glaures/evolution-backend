package expondo.evolution.planning;

import expondo.evolution.okr.Tactic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * Represents a release delivered during a timebox.
 * Each release is tied to a specific tactic and earns EVO points.
 */
@Entity
@Table(name = "timebox_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class TimeboxDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timebox_report_id", nullable = false)
    private TimeboxReport timeboxReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tactic_id", nullable = false)
    private Tactic tactic;

    /**
     * Short name/title of the release.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Jira ticket ID of the release (e.g., "EVO-1234").
     */
    @Column
    private String jiraId;

    /**
     * Brief description of the stakeholder-perceivable value delivered.
     */
    @Column(columnDefinition = "TEXT")
    private String stakeholderValue;
}
