package expondo.evolution.planning;

import expondo.evolution.okr.Tactic;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

/**
 * Snapshot of a tactic's priority and score at the moment a timebox starts.
 *
 * EVO is calculated against this snapshot, not against the live tactic, so that
 * mid-cycle re-prioritization and future changes to the scoring logic cannot
 * retroactively alter historical EVO values.
 *
 * Snapshots are created once per (timebox, tactic) when the timebox has started
 * (startDate <= today). Tactics added after that point are NOT snapshotted into
 * the running timebox and therefore cannot earn EVO until the next timebox.
 */
@Entity
@Table(name = "timebox_tactic_snapshots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"timebox_id", "tactic_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class TimeboxTacticSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timebox_id", nullable = false)
    private Timebox timebox;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tactic_id", nullable = false)
    private Tactic tactic;

    @Column(nullable = false)
    private Integer priorityAtOpen;

    /**
     * Persisted explicitly so future changes to Tactic.getScore() cannot
     * retroactively alter historical EVO calculations.
     */
    @Column(nullable = false)
    private Integer scoreAtOpen;

    @Column(nullable = false)
    private LocalDateTime capturedAt;
}
