package expondo.evolution.planning;

import expondo.evolution.okr.KeyResult;
import expondo.evolution.okr.KeyResultRepository;
import expondo.evolution.okr.Tactic;
import expondo.evolution.okr.TacticRepository;
import expondo.evolution.planning.dto.TimeboxReportDto;
import expondo.evolution.planning.dto.TimeboxReportSaveDto;
import expondo.evolution.user.Team;
import expondo.evolution.user.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimeboxReportService {

    private final TimeboxReportRepository reportRepository;
    private final TimeboxRepository timeboxRepository;
    private final TeamRepository teamRepository;
    private final TacticRepository tacticRepository;
    private final KeyResultRepository keyResultRepository;
    private final TimeboxTacticSnapshotRepository snapshotRepository;
    private final TimeboxSnapshotService snapshotService;

    public Optional<TimeboxReportDto> findByTeamAndTimebox(Long teamId, Long timeboxId) {
        return reportRepository.findByTeamIdAndTimeboxId(teamId, timeboxId)
                .map(this::toDto);
    }

    public List<TimeboxReportDto> findByTimebox(Long timeboxId) {
        return reportRepository.findByTimeboxId(timeboxId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Save or update a timebox report for a team.
     * Creates a new report if none exists, updates the existing one otherwise.
     * Throws if the timebox is closed.
     *
     * Lazily ensures priority snapshots exist before saving — robust against
     * scheduler outages or timeboxes that started today but haven't been
     * snapshotted yet.
     *
     * Validates that every delivered tactic has a snapshot for this timebox.
     * Tactics added after the timebox started have no snapshot and therefore
     * cannot earn EVO until the next timebox.
     */
    @Transactional
    public TimeboxReportDto save(Long teamId, Long timeboxId, TimeboxReportSaveDto dto) {
        Timebox timebox = timeboxRepository.findById(timeboxId)
                .orElseThrow(() -> new RuntimeException("Timebox not found: " + timeboxId));

        if (timebox.isClosed()) {
            throw new IllegalStateException("Timebox " + timebox.getNumber() + " is closed. Reports can no longer be modified.");
        }

        // Lazy snapshot trigger — handles timeboxes that started today but the
        // nightly scheduler hasn't run yet, or scheduler outages.
        snapshotService.ensureSnapshotsExist(timebox);

        // Validate that every delivered tactic was part of this timebox at start.
        if (dto.deliveries() != null) {
            for (TimeboxReportSaveDto.DeliveryEntry entry : dto.deliveries()) {
                if (snapshotRepository.findByTimeboxIdAndTacticId(timeboxId, entry.tacticId()).isEmpty()) {
                    throw new IllegalStateException(
                            "Tactic " + entry.tacticId() + " was not part of timebox " + timebox.getNumber()
                                    + " at its start and therefore cannot earn EVO. It will be available for the next timebox."
                    );
                }
            }
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        TimeboxReport report = reportRepository.findByTeamIdAndTimeboxId(teamId, timeboxId)
                .orElseGet(() -> {
                    TimeboxReport newReport = new TimeboxReport();
                    newReport.setTeam(team);
                    newReport.setTimebox(timebox);
                    return reportRepository.save(newReport);
                });

        // Update effort distribution
        report.setEffortMaintenance(dto.effortMaintenance());
        report.setEffortAdministration(dto.effortAdministration());

        // Clear old children and FLUSH so DELETEs are executed before new INSERTs
        report.getEfforts().clear();
        report.getDeliveries().clear();
        reportRepository.saveAndFlush(report);

        // Now add new efforts
        if (dto.efforts() != null) {
            for (TimeboxReportSaveDto.EffortEntry entry : dto.efforts()) {
                Tactic tactic = tacticRepository.findById(entry.tacticId())
                        .orElseThrow(() -> new RuntimeException("Tactic not found: " + entry.tacticId()));

                TimeboxEffort effort = new TimeboxEffort();
                effort.setTimeboxReport(report);
                effort.setTactic(tactic);
                effort.setEffortPercentage(entry.effortPercentage());
                report.getEfforts().add(effort);
            }
        }

        // Add new deliveries
        if (dto.deliveries() != null) {
            for (TimeboxReportSaveDto.DeliveryEntry entry : dto.deliveries()) {
                Tactic tactic = tacticRepository.findById(entry.tacticId())
                        .orElseThrow(() -> new RuntimeException("Tactic not found: " + entry.tacticId()));

                TimeboxDelivery delivery = new TimeboxDelivery();
                delivery.setTimeboxReport(report);
                delivery.setTactic(tactic);
                delivery.setName(entry.name());
                delivery.setReleaseLink(entry.releaseLink());
                delivery.setStakeholderValue(entry.stakeholderValue());

                if (entry.keyResultImpacts() != null) {
                    for (TimeboxReportSaveDto.KeyResultImpactEntry impactEntry : entry.keyResultImpacts()) {
                        KeyResult keyResult = keyResultRepository.findById(impactEntry.keyResultId())
                                .orElseThrow(() -> new RuntimeException("KeyResult not found: " + impactEntry.keyResultId()));

                        DeliveryKeyResultImpact impact = new DeliveryKeyResultImpact();
                        impact.setTimeboxDelivery(delivery);
                        impact.setKeyResult(keyResult);
                        impact.setImpactType(DeliveryKeyResultImpact.ImpactType.valueOf(impactEntry.impactType()));
                        delivery.getKeyResultImpacts().add(impact);
                    }
                }

                report.getDeliveries().add(delivery);
            }
        }

        return toDto(reportRepository.save(report));
    }

    /**
     * Close/open a timebox (admin only).
     */
    @Transactional
    public void setTimeboxClosed(Long timeboxId, boolean closed) {
        Timebox timebox = timeboxRepository.findById(timeboxId)
                .orElseThrow(() -> new RuntimeException("Timebox not found: " + timeboxId));
        timebox.setClosed(closed);
        timeboxRepository.save(timebox);
    }

    private TimeboxReportDto toDto(TimeboxReport report) {
        Long timeboxId = report.getTimebox().getId();

        List<TimeboxReportDto.EffortDto> efforts = report.getEfforts().stream()
                .map(e -> new TimeboxReportDto.EffortDto(
                        e.getId(),
                        e.getTactic().getId(),
                        e.getTactic().getCode(),
                        e.getTactic().getTitle(),
                        e.getEffortPercentage()
                ))
                .toList();

        List<TimeboxReportDto.DeliveryDto> deliveries = report.getDeliveries().stream()
                .map(d -> {
                    List<TimeboxReportDto.KeyResultImpactDto> impacts = d.getKeyResultImpacts().stream()
                            .map(impact -> new TimeboxReportDto.KeyResultImpactDto(
                                    impact.getId(),
                                    impact.getKeyResult().getId(),
                                    impact.getKeyResult().getCode(),
                                    impact.getKeyResult().getName(),
                                    impact.getImpactType().name()
                            ))
                            .toList();

                    // Read priority and score from the snapshot, falling back to
                    // the live tactic only if no snapshot exists (shouldn't happen
                    // for valid data, but keeps the UI from crashing).
                    Optional<TimeboxTacticSnapshot> snap =
                            snapshotRepository.findByTimeboxIdAndTacticId(timeboxId, d.getTactic().getId());
                    Integer priority = snap.map(TimeboxTacticSnapshot::getPriorityAtOpen)
                            .orElse(d.getTactic().getPriority());
                    int score = snap.map(TimeboxTacticSnapshot::getScoreAtOpen)
                            .orElse(d.getTactic().getScore());

                    return new TimeboxReportDto.DeliveryDto(
                            d.getId(),
                            d.getTactic().getId(),
                            d.getTactic().getCode(),
                            d.getTactic().getTitle(),
                            priority,
                            score,
                            d.getName(),
                            d.getReleaseLink(),
                            d.getStakeholderValue(),
                            impacts
                    );
                })
                .toList();

        return new TimeboxReportDto(
                report.getId(),
                report.getTeam().getId(),
                report.getTeam().getName(),
                report.getTimebox().getId(),
                report.getTimebox().getNumber(),
                report.getTimebox().isClosed(),
                report.getEffortMaintenance(),
                report.getEffortAdministration(),
                report.getBsi(),
                efforts,
                deliveries
        );
    }
}
