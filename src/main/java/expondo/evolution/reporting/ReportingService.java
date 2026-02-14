package expondo.evolution.reporting;

import expondo.evolution.planning.*;
import expondo.evolution.reporting.dto.ReportingDashboardDto;
import expondo.evolution.reporting.dto.ReportingDashboardDto.*;
import expondo.evolution.user.Team;
import expondo.evolution.user.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final TimeboxRepository timeboxRepository;
    private final TimeboxReportRepository reportRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public ReportingDashboardDto getDashboard(Long cycleId) {
        List<Timebox> timeboxes = timeboxRepository.findByCycleIdOrderByNumberAsc(cycleId);
        List<Team> teams = teamRepository.findAll();

        // Build timebox summaries
        List<TimeboxSummary> summaries = new ArrayList<>();
        List<TimeboxReport> allReports = new ArrayList<>();

        for (Timebox tb : timeboxes) {
            List<TimeboxReport> reports = reportRepository.findByTimeboxId(tb.getId());
            allReports.addAll(reports);

            List<TeamMetrics> teamMetrics = reports.stream()
                    .map(r -> new TeamMetrics(
                            r.getTeam().getId(),
                            r.getTeam().getName(),
                            r.getTeam().getColor(),
                            r.getBsi(),
                            r.getEffortMaintenance(),
                            r.getEffortAdministration(),
                            calculateEvo(r)
                    ))
                    .toList();

            summaries.add(new TimeboxSummary(
                    tb.getId(),
                    tb.getNumber(),
                    tb.isClosed(),
                    teamMetrics
            ));
        }

        // Current timebox snapshot (last non-closed timebox)
        CurrentTimeboxSnapshot currentSnapshot = buildCurrentSnapshot(timeboxes, teams);

        // Org KPIs
        OrgKpis orgKpis = buildOrgKpis(allReports);

        // Tactic investments
        List<TacticInvestment> tacticInvestments = buildTacticInvestments(allReports);

        return new ReportingDashboardDto(summaries, currentSnapshot, orgKpis, tacticInvestments);
    }

    private int calculateEvo(TimeboxReport report) {
        return report.getDeliveries().stream()
                .mapToInt(d -> d.getTactic().getScore())
                .sum();
    }

    private CurrentTimeboxSnapshot buildCurrentSnapshot(List<Timebox> timeboxes, List<Team> teams) {
        // Find the last non-closed timebox
        Timebox current = null;
        for (int i = timeboxes.size() - 1; i >= 0; i--) {
            if (!timeboxes.get(i).isClosed()) {
                current = timeboxes.get(i);
                break;
            }
        }

        if (current == null && !timeboxes.isEmpty()) {
            // All closed — use the last one
            current = timeboxes.get(timeboxes.size() - 1);
        }

        if (current == null) {
            return new CurrentTimeboxSnapshot(null, null, null, null, List.of());
        }

        List<TimeboxReport> reports = reportRepository.findByTimeboxId(current.getId());
        Map<Long, TimeboxReport> reportByTeam = reports.stream()
                .collect(Collectors.toMap(r -> r.getTeam().getId(), r -> r));

        List<TeamSnapshot> snapshots = teams.stream()
                .map(team -> {
                    TimeboxReport report = reportByTeam.get(team.getId());
                    if (report != null) {
                        return new TeamSnapshot(
                                team.getId(), team.getName(), team.getColor(),
                                true,
                                report.getBsi(),
                                report.getEffortMaintenance(),
                                report.getEffortAdministration(),
                                calculateEvo(report)
                        );
                    } else {
                        return new TeamSnapshot(
                                team.getId(), team.getName(), team.getColor(),
                                false,
                                null, null, null, 0
                        );
                    }
                })
                .toList();

        return new CurrentTimeboxSnapshot(
                current.getId(),
                current.getNumber(),
                current.getStartDate().toString(),
                current.getEndDate().toString(),
                snapshots
        );
    }

    private OrgKpis buildOrgKpis(List<TimeboxReport> allReports) {
        if (allReports.isEmpty()) {
            return new OrgKpis(BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }

        BigDecimal avgBsi = allReports.stream()
                .map(TimeboxReport::getBsi)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allReports.size()), 2, RoundingMode.HALF_UP);

        int totalEvo = allReports.stream()
                .mapToInt(this::calculateEvo)
                .sum();

        BigDecimal avgEvo = BigDecimal.valueOf(totalEvo)
                .divide(BigDecimal.valueOf(allReports.size()), 2, RoundingMode.HALF_UP);

        int totalReleases = allReports.stream()
                .mapToInt(r -> r.getDeliveries().size())
                .sum();

        return new OrgKpis(avgBsi, avgEvo, totalReleases);
    }

    private List<TacticInvestment> buildTacticInvestments(List<TimeboxReport> allReports) {
        // tacticId -> (teamId -> accumulated person days)
        Map<Long, TacticAccumulator> accumulator = new LinkedHashMap<>();

        for (TimeboxReport report : allReports) {
            int workingDays = countWorkingDays(
                    report.getTimebox().getStartDate(),
                    report.getTimebox().getEndDate()
            );
            int memberCount = report.getTeam().getMemberCount();
            BigDecimal availableDays = BigDecimal.valueOf((long) workingDays * memberCount);

            for (TimeboxEffort effort : report.getEfforts()) {
                BigDecimal personDays = availableDays
                        .multiply(effort.getEffortPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                accumulator.computeIfAbsent(effort.getTactic().getId(), id -> new TacticAccumulator(
                        effort.getTactic().getId(),
                        effort.getTactic().getCode(),
                        effort.getTactic().getTitle(),
                        effort.getTactic().getPriority()
                )).addTeamEffort(
                        report.getTeam().getId(),
                        report.getTeam().getName(),
                        report.getTeam().getColor(),
                        personDays
                );
            }
        }

        return accumulator.values().stream()
                .map(TacticAccumulator::toInvestment)
                .sorted(Comparator.comparing(TacticInvestment::tacticPriority, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private int countWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    private static class TacticAccumulator {
        final Long id;
        final String code;
        final String title;
        final Integer priority;
        final Map<Long, TeamDaysAccumulator> teamDays = new LinkedHashMap<>();

        TacticAccumulator(Long id, String code, String title, Integer priority) {
            this.id = id;
            this.code = code;
            this.title = title;
            this.priority = priority;
        }

        void addTeamEffort(Long teamId, String teamName, String teamColor, BigDecimal personDays) {
            teamDays.computeIfAbsent(teamId, k -> new TeamDaysAccumulator(teamId, teamName, teamColor))
                    .add(personDays);
        }

        TacticInvestment toInvestment() {
            List<TeamContribution> contributions = teamDays.values().stream()
                    .map(t -> new TeamContribution(t.teamId, t.teamName, t.teamColor, t.personDays))
                    .sorted(Comparator.comparing(TeamContribution::personDays).reversed())
                    .toList();
            BigDecimal total = contributions.stream()
                    .map(TeamContribution::personDays)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new TacticInvestment(id, code, title, priority, total, contributions);
        }
    }

    private static class TeamDaysAccumulator {
        final Long teamId;
        final String teamName;
        final String teamColor;
        BigDecimal personDays = BigDecimal.ZERO;

        TeamDaysAccumulator(Long teamId, String teamName, String teamColor) {
            this.teamId = teamId;
            this.teamName = teamName;
            this.teamColor = teamColor;
        }

        void add(BigDecimal days) {
            this.personDays = this.personDays.add(days);
        }
    }
}