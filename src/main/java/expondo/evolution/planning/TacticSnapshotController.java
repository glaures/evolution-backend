package expondo.evolution.planning;

import expondo.evolution.planning.dto.TacticSnapshotDto;
import expondo.evolution.planning.dto.TimeboxSnapshotSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TacticSnapshotController {

    private final TacticSnapshotService snapshotService;

    /**
     * Lists timeboxes in a cycle that have priority snapshots available
     * (used to populate the snapshot-view dropdown).
     */
    @GetMapping("/cycles/{cycleId}/timeboxes-with-snapshots")
    @PreAuthorize("hasRole('USER')")
    public List<TimeboxSnapshotSummaryDto> timeboxesWithSnapshots(@PathVariable Long cycleId) {
        return snapshotService.findTimeboxesWithSnapshots(cycleId);
    }

    /**
     * Returns the priority snapshot for a single timebox, ordered by
     * priority-at-open ascending. Archived tactics are included to keep
     * the historical ranking intact.
     */
    @GetMapping("/timeboxes/{timeboxId}/tactic-snapshots")
    @PreAuthorize("hasRole('USER')")
    public List<TacticSnapshotDto> tacticSnapshots(@PathVariable Long timeboxId) {
        return snapshotService.findSnapshot(timeboxId);
    }
}
