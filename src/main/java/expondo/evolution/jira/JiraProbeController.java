package expondo.evolution.jira;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/jira-probe")
@RequiredArgsConstructor
public class JiraProbeController {

    private final JiraProbeService jiraProbeService;

    /**
     * GET /api/admin/jira-probe/whoami
     * Returns whatever JIRA's /myself endpoint returns. 200 = auth + network OK.
     */
    @GetMapping("/whoami")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> whoami() {
        try {
            Map<String, Object> body = jiraProbeService.whoami();
            return ResponseEntity.ok(body);
        } catch (JiraProbeService.JiraProbeException e) {
            return ResponseEntity.status(502).body(Map.of(
                    "error", "JIRA probe failed",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/admin/jira-probe/issue/{key}
     * Verifies that the authenticated JIRA user can read project data.
     */
    @GetMapping("/issue/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> issue(@PathVariable String key) {
        try {
            Map<String, Object> body = jiraProbeService.getIssue(key);
            return ResponseEntity.ok(body);
        } catch (JiraProbeService.JiraProbeException e) {
            return ResponseEntity.status(502).body(Map.of(
                    "error", "JIRA probe failed",
                    "message", e.getMessage()
            ));
        }
    }
}
