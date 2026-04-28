package expondo.evolution.planning;

import expondo.evolution.okr.Tactic;
import expondo.evolution.okr.TacticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Creates priority snapshots for the start of each timebox.
 *
 * Called from three places:
 * - {@link TimeboxSnapshotScheduler} runs nightly for all timeboxes that have started.
 * - {@link TimeboxReportService} as a lazy fallback when a report is being saved.
 * - The application startup runner for backfilling historical timeboxes.
 *
 * Archived tactics are excluded from snapshotting — they cannot earn EVO in
 * future timeboxes. Existing snapshots for archived tactics remain intact, so
 * historical reports continue to display them correctly.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeboxSnapshotService {

    private final TimeboxTacticSnapshotRepository snapshotRepository;
    private final TacticRepository tacticRepository;

    @Transactional
    public int ensureSnapshotsExist(Timebox timebox) {
        if (timebox.getStartDate().isAfter(LocalDate.now())) {
            return 0;
        }
        if (snapshotRepository.existsByTimeboxId(timebox.getId())) {
            return 0;
        }
        return createSnapshots(timebox);
    }

    @Transactional
    public int backfillSnapshots(Timebox timebox) {
        if (snapshotRepository.existsByTimeboxId(timebox.getId())) {
            return 0;
        }
        return createSnapshots(timebox);
    }

    private int createSnapshots(Timebox timebox) {
        List<Tactic> tactics = tacticRepository.findByArchivedFalse();
        LocalDateTime now = LocalDateTime.now();
        int created = 0;

        for (Tactic tactic : tactics) {
            if (tactic.getPriority() == null) {
                continue;
            }
            TimeboxTacticSnapshot snapshot = new TimeboxTacticSnapshot();
            snapshot.setTimebox(timebox);
            snapshot.setTactic(tactic);
            snapshot.setPriorityAtOpen(tactic.getPriority());
            snapshot.setScoreAtOpen(tactic.getScore());
            snapshot.setCapturedAt(now);
            snapshotRepository.save(snapshot);
            created++;
        }

        log.info("Created {} priority snapshots for timebox {} (cycle {}, number {})",
                created, timebox.getId(), timebox.getCycle().getId(), timebox.getNumber());
        return created;
    }
}
