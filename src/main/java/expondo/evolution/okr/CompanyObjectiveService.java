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

    public List<CompanyObjectiveDto> findByCycleId(Long cycleId) {
        List<CompanyObjectiveDto> objectives = objectiveRepository.findByCycleIdOrderByCodeAsc(cycleId).stream()
                .map(objectiveMapper::toDto)
                .toList();

        // Enrich tactics with activity status
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
        objectiveMapper.updateEntity(dto, objective);
        return objectiveMapper.toDto(objectiveRepository.save(objective));
    }

    @Transactional
    public void delete(Long id) {
        if (!objectiveRepository.existsById(id)) {
            throw new RuntimeException("Objective not found: " + id);
        }
        objectiveRepository.deleteById(id);
    }

    /**
     * Build a map of tacticId -> activityStatus for all tactics in a cycle.
     * Based on coverage: in how many of the cycle's timeboxes was effort reported?
     *
     * HIGH   = effort in >60% of the cycle's timeboxes
     * MEDIUM = effort in 30-60% of the cycle's timeboxes
     * LOW    = effort in <30% of the cycle's timeboxes (but at least once)
     * null   = no effort ever reported
     */
    private Map<Long, String> buildActivityMap(Long cycleId) {
        List<Timebox> timeboxes = timeboxRepository.findByCycleIdOrderByNumberAsc(cycleId);
        int totalTimeboxes = timeboxes.size();
        if (totalTimeboxes == 0) return Map.of();

        // Get the number of distinct timeboxes with effort per tactic
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