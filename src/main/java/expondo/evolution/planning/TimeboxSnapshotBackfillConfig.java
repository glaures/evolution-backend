package expondo.evolution.planning;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

/**
 * Backfills priority snapshots for timeboxes that already started before this
 * feature was deployed.
 *
 * Idempotent — uses the snapshot service's existsByTimeboxId check, so re-running
 * the application has no effect once snapshots are in place.
 *
 * Caveat: For historical timeboxes, the backfill uses the CURRENT priority of each
 * tactic, since we have no way to reconstruct the priority that was in effect when
 * those timeboxes started. This is documented as an acceptable approximation.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TimeboxSnapshotBackfillConfig {

    @Bean
    public ApplicationRunner timeboxSnapshotBackfillRunner(
            TimeboxRepository timeboxRepository,
            TimeboxSnapshotService snapshotService
    ) {
        return args -> {
            LocalDate today = LocalDate.now();
            List<Timebox> all = timeboxRepository.findAll();

            int totalCreated = 0;
            int touchedTimeboxes = 0;
            for (Timebox tb : all) {
                if (tb.getStartDate().isAfter(today)) continue;
                int created = snapshotService.backfillSnapshots(tb);
                if (created > 0) {
                    touchedTimeboxes++;
                    totalCreated += created;
                }
            }

            if (touchedTimeboxes > 0) {
                log.warn("Snapshot backfill: created {} snapshot(s) across {} timebox(es) " +
                        "using CURRENT tactic priorities (historical priorities unknown).",
                        totalCreated, touchedTimeboxes);
            } else {
                log.info("Snapshot backfill: nothing to do, all started timeboxes already have snapshots.");
            }
        };
    }
}
