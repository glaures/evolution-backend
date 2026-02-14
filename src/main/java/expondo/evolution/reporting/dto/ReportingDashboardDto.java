package expondo.evolution.reporting.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReportingDashboardDto(
        /**
         * BSI and EVO per team per timebox, ordered by timebox number.
         */
        List<TimeboxSummary> timeboxSummaries,

        /**
         * Aggregated data for the current (last open) timebox.
         */
        CurrentTimeboxSnapshot currentTimebox,

        /**
         * Org-level KPIs across the full cycle.
         */
        OrgKpis orgKpis,

        /**
         * Effort invested per tactic across the full cycle.
         */
        List<TacticInvestment> tacticInvestments
) {
    public record TimeboxSummary(
            Long timeboxId,
            Integer timeboxNumber,
            boolean closed,
            List<TeamMetrics> teams
    ) {}

    public record TeamMetrics(
            Long teamId,
            String teamName,
            String teamColor,
            BigDecimal bsi,
            BigDecimal effortMaintenance,
            BigDecimal effortAdministration,
            int evo
    ) {}

    public record CurrentTimeboxSnapshot(
            Long timeboxId,
            Integer timeboxNumber,
            String startDate,
            String endDate,
            List<TeamSnapshot> teams
    ) {}

    public record TeamSnapshot(
            Long teamId,
            String teamName,
            String teamColor,
            boolean hasReport,
            BigDecimal bsi,
            BigDecimal effortMaintenance,
            BigDecimal effortAdministration,
            int evo
    ) {}

    public record OrgKpis(
            BigDecimal avgBsi,
            BigDecimal avgEvo,
            int totalReleases
    ) {}

    public record TacticInvestment(
            Long tacticId,
            String tacticCode,
            String tacticTitle,
            Integer tacticPriority,
            BigDecimal totalPersonDays,
            List<TeamContribution> teamContributions
    ) {}

    public record TeamContribution(
            Long teamId,
            String teamName,
            String teamColor,
            BigDecimal personDays
    ) {}
}