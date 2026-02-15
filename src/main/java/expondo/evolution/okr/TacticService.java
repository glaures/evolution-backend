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
import expondo.evolution.user.Unit;
import expondo.evolution.user.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TacticService {

    private final TacticRepository tacticRepository;
    private final CompanyObjectiveRepository objectiveRepository;
    private final UnitRepository unitRepository;
    private final TacticMapper tacticMapper;
    private final TimeboxDeliveryRepository deliveryRepository;
    private final TimeboxEffortRepository effortRepository;

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

        Tactic tactic = tacticMapper.toEntity(dto);
        tactic.setCompanyObjective(objective);

        long count = tacticRepository.findByCompanyObjectiveIdOrderByPriorityAsc(objectiveId).size();
        tactic.setCode(objective.getCode() + ".T" + (count + 1));

        if (dto.responsibleUnitId() != null) {
            Unit unit = unitRepository.findById(dto.responsibleUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found: " + dto.responsibleUnitId()));
            tactic.setResponsibleUnit(unit);
        }

        // Priority is always cycle-wide
        Long cycleId = objective.getCycle().getId();
        assignPriority(tactic, dto.priority(), cycleId);

        return tacticMapper.toDto(tacticRepository.save(tactic));
    }

    public List<TacticDto> findByCycleId(Long cycleId) {
        return tacticRepository.findByCycleIdOrderByPriorityAsc(cycleId).stream()
                .map(tacticMapper::toDto)
                .toList();
    }

    @Transactional
    public List<TacticDto> reorder(Long cycleId, List<Long> tacticIds) {
        for (int i = 0; i < tacticIds.size(); i++) {
            Tactic tactic = tacticRepository.findById(tacticIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Tactic not found"));
            tactic.setPriority(i + 1);
            tacticRepository.save(tactic);
        }
        return findByCycleId(cycleId);
    }

    @Transactional
    public TacticDto update(Long id, TacticUpdateDto dto) {
        Tactic tactic = tacticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tactic not found: " + id));
        tacticMapper.updateEntity(dto, tactic);

        // Handle objective reassignment
        if (dto.companyObjectiveId() != null
                && !dto.companyObjectiveId().equals(tactic.getCompanyObjective().getId())) {
            CompanyObjective newObjective = objectiveRepository.findById(dto.companyObjectiveId())
                    .orElseThrow(() -> new RuntimeException("Objective not found: " + dto.companyObjectiveId()));
            tactic.setCompanyObjective(newObjective);
        }

        if (dto.responsibleUnitId() != null) {
            Unit unit = unitRepository.findById(dto.responsibleUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found: " + dto.responsibleUnitId()));
            tactic.setResponsibleUnit(unit);
        } else {
            tactic.setResponsibleUnit(null);
        }

        return tacticMapper.toDto(tacticRepository.save(tactic));
    }

    @Transactional
    public void delete(Long id) {
        if (!tacticRepository.existsById(id)) {
            throw new RuntimeException("Tactic not found: " + id);
        }
        tacticRepository.deleteById(id);
    }

    /**
     * Get activity data (releases and efforts) for a tactic.
     * Loaded on demand when a user expands a tactic in the objectives overview.
     * Efforts are converted to person days using the same calculation as the reporting dashboard.
     */
    @Transactional(readOnly = true)
    public TacticActivityDto getActivity(Long tacticId) {
        List<TimeboxDelivery> deliveries = deliveryRepository.findByTacticIdWithDetails(tacticId);
        List<TimeboxEffort> efforts = effortRepository.findByTacticIdWithDetails(tacticId);

        List<TacticActivityDto.ReleaseEntry> releaseEntries = deliveries.stream()
                .map(d -> new TacticActivityDto.ReleaseEntry(
                        d.getId(),
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

        List<TacticActivityDto.EffortEntry> effortEntries = efforts.stream()
                .map(e -> {
                    BigDecimal personDays = PersonDaysCalculator.calculate(
                            e.getTimeboxReport().getTimebox().getStartDate(),
                            e.getTimeboxReport().getTimebox().getEndDate(),
                            e.getTimeboxReport().getTeam().getMemberCount(),
                            e.getEffortPercentage()
                    );
                    return new TacticActivityDto.EffortEntry(
                            e.getTimeboxReport().getTimebox().getNumber(),
                            e.getTimeboxReport().getTeam().getName(),
                            e.getTimeboxReport().getTeam().getColor(),
                            personDays
                    );
                })
                .toList();

        BigDecimal totalPersonDays = effortEntries.stream()
                .map(TacticActivityDto.EffortEntry::personDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Last activity: most recent effort entry (already sorted by timebox number ASC)
        String lastTimebox = null;
        String lastTeam = null;
        if (!efforts.isEmpty()) {
            TimeboxEffort last = efforts.getLast();
            lastTimebox = "Timebox " + last.getTimeboxReport().getTimebox().getNumber();
            lastTeam = last.getTimeboxReport().getTeam().getName();
        }

        return new TacticActivityDto(
                tacticId,
                totalPersonDays,
                lastTimebox,
                lastTeam,
                releaseEntries,
                effortEntries
        );
    }

    /**
     * Assign priority to a tactic within its cycle.
     * If an explicit priority is given, shift existing tactics down.
     * If no priority is given (null), assign the lowest (max + 1) across the entire cycle.
     */
    private void assignPriority(Tactic tactic, Integer requestedPriority, Long cycleId) {
        if (requestedPriority != null && requestedPriority > 0) {
            // Explicit priority: shift all cycle tactics at this priority or lower down by 1
            List<Tactic> existing = tacticRepository.findByCycleIdOrderByPriorityAsc(cycleId);
            for (Tactic t : existing) {
                if (t.getPriority() != null && t.getPriority() >= requestedPriority) {
                    t.setPriority(t.getPriority() + 1);
                    tacticRepository.save(t);
                }
            }
            tactic.setPriority(requestedPriority);
        } else {
            // No priority: assign lowest across entire cycle
            Integer maxPriority = tacticRepository.findMaxPriorityByCycleId(cycleId);
            tactic.setPriority(maxPriority != null ? maxPriority + 1 : 1);
        }
    }
}