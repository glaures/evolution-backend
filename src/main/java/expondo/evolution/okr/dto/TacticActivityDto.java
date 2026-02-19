package expondo.evolution.okr.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated activity data for a single tactic within a cycle.
 * Loaded on demand when expanding a tactic in the objectives overview.
 */
public record TacticActivityDto(
        Long tacticId,
        BigDecimal totalPersonDays,
        List<TeamContribution> teamContributions,
        List<ReleaseEntry> releases
) {
    public record TeamContribution(
            String teamName,
            String teamColor,
            BigDecimal personDays
    ) {}

    public record ReleaseEntry(
            Long id,
            Integer timeboxNumber,
            String teamName,
            String teamColor,
            List<KeyResultImpactEntry> keyResultImpacts
    ) {}

    public record KeyResultImpactEntry(
            String keyResultCode,
            String keyResultName,
            String impactType
    ) {}
}