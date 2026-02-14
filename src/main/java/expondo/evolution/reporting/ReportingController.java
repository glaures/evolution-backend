package expondo.evolution.reporting;

import expondo.evolution.reporting.dto.ReportingDashboardDto;
import lombok.RequiredArgsConstructor;import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/cycles/{cycleId}/reporting")
    public ReportingDashboardDto getDashboard(@PathVariable Long cycleId) {
        return reportingService.getDashboard(cycleId);
    }
}
