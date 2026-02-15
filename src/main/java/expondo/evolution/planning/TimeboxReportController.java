package expondo.evolution.planning;

import expondo.evolution.planning.dto.TimeboxReportDto;
import expondo.evolution.planning.dto.TimeboxReportSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TimeboxReportController {

    private final TimeboxReportService reportService;

    /**
     * Get a specific report for a team and timebox.
     */
    @GetMapping("/teams/{teamId}/timeboxes/{timeboxId}/report")
    public ResponseEntity<TimeboxReportDto> getReport(
            @PathVariable Long teamId,
            @PathVariable Long timeboxId) {
        return reportService.findByTeamAndTimebox(teamId, timeboxId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Get all reports for a timebox.
     */
    @GetMapping("/timeboxes/{timeboxId}/reports")
    public List<TimeboxReportDto> getReportsByTimebox(@PathVariable Long timeboxId) {
        return reportService.findByTimebox(timeboxId);
    }

    /**
     * Save or update a report for a team and timebox.
     */
    @PutMapping("/teams/{teamId}/timeboxes/{timeboxId}/report")
    public TimeboxReportDto saveReport(
            @PathVariable Long teamId,
            @PathVariable Long timeboxId,
            @RequestBody TimeboxReportSaveDto dto) {
        return reportService.save(teamId, timeboxId, dto);
    }

    /**
     * Close or reopen a timebox (admin only).
     */
    @PutMapping("/timeboxes/{timeboxId}/closed")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void setTimeboxClosed(
            @PathVariable Long timeboxId,
            @RequestBody Map<String, Boolean> body) {
        reportService.setTimeboxClosed(timeboxId, body.getOrDefault("closed", true));
    }
}