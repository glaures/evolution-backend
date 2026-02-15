package expondo.evolution.planning.dto;

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
            String releaseLink,
            String stakeholderValue,
            List<KeyResultImpactDto> keyResultImpacts
    ) {}

    /**
     * Read-only representation of a KR impact linked to a delivery.
     */
    public record KeyResultImpactDto(
            Long id,
            Long keyResultId,
            String keyResultCode,
            String keyResultName,
            String impactType // "IMPACTS" or "ACHIEVES"
    ) {}
}