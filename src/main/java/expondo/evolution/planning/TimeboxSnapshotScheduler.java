package expondo.evolution.planning;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Runs nightly to ensure that any timebox which has started by today has its
 * priority snapshots in place. Idempotent — safe to run multiple times per day.
 *
 * Requires @EnableScheduling on the application class.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeboxSnapshotScheduler {

    private final TimeboxRepository timeboxRepository;
    private final TimeboxSnapshotService snapshotService;

    /**
     * Runs daily at 00:05 server time.
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void snapshotStartedTimeboxes() {
        LocalDate today = LocalDate.now();
        List<Timebox> all = timeboxRepository.findAll();

        int processed = 0;
        for (Timebox tb : all) {
            if (tb.getStartDate().isAfter(today)) continue;
            int created = snapshotService.ensureSnapshotsExist(tb);
            if (created > 0) processed++;
        }

        if (processed > 0) {
            log.info("Snapshot scheduler: created snapshots for {} timebox(es)", processed);
        }
    }
}
