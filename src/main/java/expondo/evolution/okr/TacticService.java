package expondo.evolution.okr;

import expondo.evolution.okr.dto.TacticActivityDto;
import expondo.evolution.okr.dto.TacticCreateDto;
import expondo.evolution.okr.dto.TacticDto;
import expondo.evolution.okr.dto.TacticUpdateDto;
import expondo.evolution.okr.mapper.TacticMapper;
import expondo.evolution.planning.TimeboxDelivery;
import expondo.evolution.planning.TimeboxDeliveryRepository;
import expondo.evolution.planning.TimeboxEffort;
import expondo.evolution.planning.TimeboxEffortRepository;
import expondo.evolution.planning.PersonDaysCalculator;
import expondo.evolution.planning.TimeboxTacticSnapshotRepository;
import expondo.evolution.user.Unit;
import expondo.evolution.user.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TacticService {

    private final TacticRepository tacticRepository;
    private final CompanyObjectiveRepository objectiveRepository;
    private final UnitRepository unitRepository;
    private final TacticMapper tacticMapper;
    private final TimeboxDeliveryRepository deliveryRepository;
    private final TimeboxEffortRepository effortRepository;
    private final TimeboxTacticSnapshotRepository snapshotRepository;

    public List<TacticDto> findByObjectiveId(Long objectiveId) {
        return tacticRepository.findByCompanyObjectiveIdOrderByPriorityAsc(objectiveId).stream()
                .map(tacticMapper::toDto)
                .toList();
    }

    public TacticDto findById(Long id) {
        return tacticRepository.findById(id)
                .map(tacticMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Tactic not found: " + id));
    }

    @Transactional
    public TacticDto create(Long objectiveId, TacticCreateDto dto) {
        CompanyObjective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new RuntimeException("Objective not found: " + objectiveId));

        if (objective.isArchived()) {
            throw new IllegalStateException("Cannot create tactic on archived objective " + objective.getCode());
        }

        Tactic tactic = tacticMapper.toEntity(dto);
        tactic.setCompanyObjective(objective);

        long count = tacticRepository.findByCompanyObjectiveIdOrderByPriorityAsc(objectiveId).size();
        tactic.setCode(objective.getCode() + ".T" + (count + 1));

        if (dto.responsibleUnitId() != null) {
            Unit unit = unitRepository.findById(dto.responsibleUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found: " + dto.responsibleUnitId()));
            tactic.setResponsibleUnit(unit);
        }

        Long cycleId = objective.getCycle().getId();
        assignPriority(tactic, dto.priority(), cycleId);
        tactic.setPriorityChangedAt(LocalDateTime.now());

        return tacticMapper.toDto(tacticRepository.save(tactic));
    }

    public List<TacticDto> findByCycleId(Long cycleId) {
        return tacticRepository.findByCycleIdOrderByPriorityAsc(cycleId).stream()
                .map(tacticMapper::toDto)
                .toList();
    }

    @Transactional
    public List<TacticDto> reorder(Long cycleId, List<Long> tacticIds) {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < tacticIds.size(); i++) {
            Tactic tactic = tacticRepository.findById(tacticIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Tactic not found"));
            if (tactic.isArchived()) {
                throw new IllegalStateException("Archived tactic " + tactic.getCode() + " cannot be reordered");
            }
            int newPriority = i + 1;
            if (tactic.getPriority() == null || tactic.getPriority() != newPriority) {
                tactic.setPriority(newPriority);
                tactic.setPriorityChangedAt(now);
                tacticRepository.save(tactic);
            }
        }
        return findByCycleId(cycleId);
    }

    @Transactional
    public TacticDto update(Long id, TacticUpdateDto dto) {
        Tactic tactic = tacticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tactic not found: " + id));

        if (tactic.isArchived()) {
            throw new IllegalStateException("Cannot update archived tactic " + tactic.getCode());
        }

        Integer priorityBefore = tactic.getPriority();
        tacticMapper.updateEntity(dto, tactic);

        if (dto.companyObjectiveId() != null
                && !dto.companyObjectiveId().equals(tactic.getCompanyObjective().getId())) {
            CompanyObjective newObjective = objectiveRepository.findById(dto.companyObjectiveId())
                    .orElseThrow(() -> new RuntimeException("Objective not found: " + dto.companyObjectiveId()));
            if (newObjective.isArchived()) {
                throw new IllegalStateException("Cannot reassign tactic to archived objective " + newObjective.getCode());
            }
            tactic.setCompanyObjective(newObjective);
        }

        if (dto.responsibleUnitId() != null) {
            Unit unit = unitRepository.findById(dto.responsibleUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found: " + dto.responsibleUnitId()));
            tactic.setResponsibleUnit(unit);
        } else {
            tactic.setResponsibleUnit(null);
        }

        if (!Objects.equals(priorityBefore, tactic.getPriority())) {
            tactic.setPriorityChangedAt(LocalDateTime.now());
        }

        return tacticMapper.toDto(tacticRepository.save(tactic));
    }

    @Transactional
    public TacticDto archive(Long id) {
        Tactic tactic = tacticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tactic not found: " + id));
        tactic.setArchived(true);
        return tacticMapper.toDto(tacticRepository.save(tactic));
    }

    @Transactional
    public void delete(Long id) {
        Tactic tactic = tacticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tactic not found: " + id));

        boolean hasSnapshots = snapshotRepository.existsByTacticId(id);
        boolean hasDeliveries = deliveryRepository.existsByTacticId(id);
        boolean hasEfforts = effortRepository.existsByTacticId(id);

        if (hasSnapshots || hasDeliveries || hasEfforts) {
            throw new IllegalStateException(
                    "Tactic " + tactic.getCode() + " has historical data and cannot be deleted. " +
                            "Use archive instead."
            );
        }

        tacticRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TacticActivityDto getActivity(Long tacticId) {
        List<TimeboxDelivery> deliveries = deliveryRepository.findByTacticIdWithDetails(tacticId);
        List<TimeboxEffort> efforts = effortRepository.findByTacticIdWithDetails(tacticId);

        List<TacticActivityDto.ReleaseEntry> releaseEntries = deliveries.stream()
                .map(d -> new TacticActivityDto.ReleaseEntry(
                        d.getId(),
                        d.getName(),
                        d.getReleaseLink(),
                        d.getTimeboxReport().getTimebox().getNumber(),
                        d.getTimeboxReport().getTeam().getName(),
                        d.getTimeboxReport().getTeam().getColor(),
                        d.getKeyResultImpacts().stream()
                                .map(i -> new TacticActivityDto.KeyResultImpactEntry(
                                        i.getKeyResult().getCode(),
                                        i.getKeyResult().getName(),
                                        i.getImpactType().name()
                                ))
                                .toList()
                ))
                .toList();

        Map<String, TeamAccumulator> teamMap = new LinkedHashMap<>();
        for (TimeboxEffort e : efforts) {
            BigDecimal personDays = PersonDaysCalculator.calculate(
                    e.getTimeboxReport().getTimebox().getStartDate(),
                    e.getTimeboxReport().getTimebox().getEndDate(),
                    e.getTimeboxReport().getTeam().getMemberCount(),
                    e.getEffortPercentage()
            );
            String teamName = e.getTimeboxReport().getTeam().getName();
            teamMap.computeIfAbsent(teamName, k -> new TeamAccumulator(
                    teamName, e.getTimeboxReport().getTeam().getColor()
            )).add(personDays);
        }

        List<TacticActivityDto.TeamContribution> contributions = teamMap.values().stream()
                .map(a -> new TacticActivityDto.TeamContribution(a.name, a.color, a.personDays))
                .sorted(Comparator.comparing(TacticActivityDto.TeamContribution::personDays).reversed())
                .toList();

        BigDecimal totalPersonDays = contributions.stream()
                .map(TacticActivityDto.TeamContribution::personDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TacticActivityDto(tacticId, totalPersonDays, contributions, releaseEntries);
    }

    private static class TeamAccumulator {
        final String name;
        final String color;
        BigDecimal personDays = BigDecimal.ZERO;

        TeamAccumulator(String name, String color) {
            this.name = name;
            this.color = color;
        }

        void add(BigDecimal days) {
            this.personDays = this.personDays.add(days);
        }
    }

    private void assignPriority(Tactic tactic, Integer requestedPriority, Long cycleId) {
        if (requestedPriority != null && requestedPriority > 0) {
            List<Tactic> existing = tacticRepository.findByCycleIdOrderByPriorityAsc(cycleId);
            for (Tactic t : existing) {
                if (t.getPriority() != null && t.getPriority() >= requestedPriority) {
                    t.setPriority(t.getPriority() + 1);
                    t.setPriorityChangedAt(LocalDateTime.now());
                    tacticRepository.save(t);
                }
            }
            tactic.setPriority(requestedPriority);
        } else {
            Integer maxPriority = tacticRepository.findMaxPriorityByCycleId(cycleId);
            tactic.setPriority(maxPriority != null ? maxPriority + 1 : 1);
        }
    }
}
