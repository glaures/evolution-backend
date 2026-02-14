package expondo.evolution.planning.dto;

import java.math.BigDecimal;
import java.util.List;

public record TimeboxReportSaveDto(
        BigDecimal effortMaintenance,
        BigDecimal effortAdministration,
        List<EffortEntry> efforts,
        List<DeliveryEntry> deliveries
) {
    public record EffortEntry(
            Long tacticId,
            BigDecimal effortPercentage
    ) {}

    public record DeliveryEntry(
            Long id,
            Long tacticId,
            String name,
            String jiraId,
            String stakeholderValue
    ) {}
}
