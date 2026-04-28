package expondo.evolution.okr;

import expondo.evolution.okr.dto.CompanyObjectiveCreateDto;
import expondo.evolution.okr.dto.CompanyObjectiveDto;
import expondo.evolution.okr.dto.CompanyObjectiveUpdateDto;
import expondo.evolution.okr.dto.TacticDto;
import expondo.evolution.okr.mapper.CompanyObjectiveMapper;
import expondo.evolution.planning.TimeboxEffortRepository;
import expondo.evolution.planning.TimeboxRepository;
import expondo.evolution.planning.Timebox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompanyObjectiveService {

    private final CompanyObjectiveRepository objectiveRepository;
    private final CycleRepository cycleRepository;
    private final CompanyObjectiveMapper objectiveMapper;
    private final TimeboxEffortRepository effortRepository;
    private final TimeboxRepository timeboxRepository;
    private final TacticRepository tacticRepository;

    public List<CompanyObjectiveDto> findByCycleId(Long cycleId) {
        List<CompanyObjectiveDto> objectives = objectiveRepository.findByCycleIdOrderByCodeAsc(cycleId).stream()
                .map(objectiveMapper::toDto)
                .toList();

        Map<Long, String> activityMap = buildActivityMap(cycleId);

        return objectives.stream()
                .map(obj -> new CompanyObjectiveDto(
                        obj.id(), obj.code(), obj.name(), obj.description(), obj.cycleId(),
                        obj.keyResults(),
                        obj.tactics().stream()
                                .map(t -> new TacticDto(
                                        t.id(), t.code(), t.title(), t.description(),
                                        t.priority(), t.score(), t.companyObjectiveId(),
                                        t.responsibleUnitId(), t.responsibleUnitName(),
                                        activityMap.get(t.id())
                                ))
                                .toList()
                ))
                .toList();
    }

    public CompanyObjectiveDto findById(Long id) {
        return objectiveRepository.findById(id)
                .map(objectiveMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Objective not found: " + id));
    }

    @Transactional
    public CompanyObjectiveDto create(Long cycleId, CompanyObjectiveCreateDto dto) {
        Cycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Cycle not found: " + cycleId));

        CompanyObjective objective = objectiveMapper.toEntity(dto);
        objective.setCycle(cycle);

        long count = objectiveRepository.findByCycleIdOrderByCodeAsc(cycleId).size();
        objective.setCode("CO" + (count + 1));

        return objectiveMapper.toDto(objectiveRepository.save(objective));
    }

    @Transactional
    public CompanyObjectiveDto update(Long id, CompanyObjectiveUpdateDto dto) {
        CompanyObjective objective = objectiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Objective not found: " + id));
        if (objective.isArchived()) {
            throw new IllegalStateException("Cannot update archived objective " + objective.getCode());
        }
        objectiveMapper.updateEntity(dto, objective);
        return objectiveMapper.toDto(objectiveRepository.save(objective));
    }

    /**
     * Soft-delete: marks the CO as archived. Refuses if any active tactics
     * still belong to the CO — they must be archived or reassigned first.
     */
    @Transactional
    public CompanyObjectiveDto archive(Long id) {
        CompanyObjective objective = objectiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Objective not found: " + id));

        long activeCount = tacticRepository.countActiveByObjectiveId(id);
        if (activeCount > 0) {
            throw new IllegalStateException(
                    "Cannot archive objective " + objective.getCode() + ": it has " + activeCount +
                            " active tactic(s). Reassign or archive them first."
            );
        }

        objective.setArchived(true);
        return objectiveMapper.toDto(objectiveRepository.save(objective));
    }

    /**
     * Hard delete. Refuses if the CO has any tactics, even archived ones —
     * cascading the delete would also delete archived tactics that may still
     * be referenced from historical reports.
     */
    @Transactional
    public void delete(Long id) {
        CompanyObjective objective = objectiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Objective not found: " + id));

        long totalTactics = tacticRepository.countByCompanyObjectiveId(id);
        if (totalTactics > 0) {
            throw new IllegalStateException(
                    "Cannot delete objective " + objective.getCode() + ": it has " + totalTactics +
                            " tactic(s). Delete or reassign them first."
            );
        }

        objectiveRepository.deleteById(id);
    }

    /**
     * Build a map of tacticId -> activityStatus for all tactics in a cycle.
     */
    private Map<Long, String> buildActivityMap(Long cycleId) {
        List<Timebox> timeboxes = timeboxRepository.findByCycleIdOrderByNumberAsc(cycleId);
        int totalTimeboxes = timeboxes.size();
        if (totalTimeboxes == 0) return Map.of();

        List<Object[]> coverageData = effortRepository.findEffortCoverageByCycleId(cycleId);

        Map<Long, String> result = new HashMap<>();
        for (Object[] row : coverageData) {
            Long tacticId = (Long) row[0];
            long activeTimeboxes = (Long) row[1];

            double ratio = (double) activeTimeboxes / totalTimeboxes;
            if (ratio > 0.6) {
                result.put(tacticId, "HIGH");
            } else if (ratio >= 0.3) {
                result.put(tacticId, "MEDIUM");
            } else {
                result.put(tacticId, "LOW");
            }
        }
        return result;
    }
}
