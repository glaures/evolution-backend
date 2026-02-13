package expondo.evolution.okr;

import expondo.evolution.okr.dto.TacticCreateDto;
import expondo.evolution.okr.dto.TacticDto;
import expondo.evolution.okr.dto.TacticUpdateDto;
import expondo.evolution.okr.mapper.TacticMapper;
import expondo.evolution.user.Unit;
import expondo.evolution.user.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TacticService {

    private final TacticRepository tacticRepository;
    private final CompanyObjectiveRepository objectiveRepository;
    private final UnitRepository unitRepository;
    private final TacticMapper tacticMapper;

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

        // Priority handling
        if (dto.priority() != null) {
            // Explicit priority: shift all tactics at this priority or lower down by 1
            List<Tactic> existing = tacticRepository.findByCompanyObjectiveIdOrderByPriorityAsc(objectiveId);
            for (Tactic t : existing) {
                if (t.getPriority() != null && t.getPriority() >= dto.priority()) {
                    t.setPriority(t.getPriority() + 1);
                    tacticRepository.save(t);
                }
            }
            tactic.setPriority(dto.priority());
        } else {
            // No priority given: assign lowest (highest number + 1)
            Integer maxPriority = tacticRepository.findMaxPriorityByObjectiveId(objectiveId);
            tactic.setPriority(maxPriority != null ? maxPriority + 1 : 1);
        }

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
}