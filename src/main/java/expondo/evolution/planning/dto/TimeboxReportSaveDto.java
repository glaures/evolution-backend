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
            String releaseLink,
            String stakeholderValue,
            List<KeyResultImpactEntry> keyResultImpacts
    ) {}

    /**
     * Links a delivery to a Key Result with an impact type.
     */
    public record KeyResultImpactEntry(
            Long keyResultId,
            String impactType // "IMPACTS" or "ACHIEVES"
    ) {}
}