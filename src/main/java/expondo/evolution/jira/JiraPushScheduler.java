package expondo.evolution.jira;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls every 30 seconds for tactics whose priority has changed in Evolution
 * but hasn't been pushed to JIRA yet, and the configured quiet period has elapsed.
 *
 * Requires @EnableScheduling (already on for the Phase 1 snapshot scheduler).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JiraPushScheduler {

    private final JiraPushService pushService;

    @Scheduled(fixedDelay = 30_000)
    public void pushPending() {
        try {
            pushService.pushPendingPriorities();
        } catch (Exception e) {
            log.error("JIRA push tick failed", e);
        }
    }
}
