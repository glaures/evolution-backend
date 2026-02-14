package expondo.evolution.planning;

import java.math.BigDecimal;
import java.util.List;

public record TimeboxReportDto(
        Long id,
        Long teamId,
        String teamName,
        Long timeboxId,
        Integer timeboxNumber,
        boolean timeboxClosed,
        BigDecimal effortMaintenance,
        BigDecimal effortAdministration,
        BigDecimal bsi,
        List<EffortDto> efforts,
        List<DeliveryDto> deliveries
) {
    public record EffortDto(
            Long id,
            Long tacticId,
            String tacticCode,
            String tacticTitle,
            BigDecimal effortPercentage
    ) {}

    public record DeliveryDto(
            Long id,
            Long tacticId,
            String tacticCode,
            String tacticTitle,
            Integer tacticPriority,
            Integer tacticScore,
            String name,
            String jiraId,
            String stakeholderValue
    ) {}
}
