package expondo.evolution.jira;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/jira-sync")
@RequiredArgsConstructor
public class JiraSyncController {

    private final JiraSyncService syncService;
    private final JiraSyncRunRepository runRepository;

    /**
     * Triggers a sync run synchronously. Returns the run record once finished.
     * Note: with a few dozen tactics this is fast (a couple of seconds). If the
     * tactic count grows into the thousands, consider moving to async.
     */
    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    public JiraSyncRunDto run() {
        return JiraSyncRunDto.from(syncService.runSync());
    }

    @GetMapping("/runs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<JiraSyncRunDto> recentRuns(@RequestParam(defaultValue = "20") int limit) {
        return runRepository.findAllByOrderByStartedAtDesc(PageRequest.of(0, Math.min(limit, 100)))
                .stream()
                .map(JiraSyncRunDto::from)
                .toList();
    }

    @GetMapping("/runs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public JiraSyncRunDto run(@PathVariable Long id) {
        return runRepository.findById(id)
                .map(JiraSyncRunDto::from)
                .orElseThrow(() -> new RuntimeException("Run not found: " + id));
    }
}
