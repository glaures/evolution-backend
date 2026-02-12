package expondo.evolution.okr;

import expondo.evolution.okr.dto.CompanyObjectiveCreateDto;
import expondo.evolution.okr.dto.CompanyObjectiveDto;
import expondo.evolution.okr.dto.CompanyObjectiveUpdateDto;
import expondo.evolution.okr.mapper.CompanyObjectiveMapper;
import expondo.evolution.user.Unit;
import expondo.evolution.user.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyObjectiveService {

    private final CompanyObjectiveRepository objectiveRepository;
    private final CycleRepository cycleRepository;
    private final UnitRepository unitRepository;
    private final CompanyObjectiveMapper objectiveMapper;

    public List<CompanyObjectiveDto> findByCycleId(Long cycleId) {
        return objectiveRepository.findByCycleIdOrderByCodeAsc(cycleId).stream()
                .map(objectiveMapper::toDto)
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

        if (dto.ownerUnitId() != null) {
            Unit unit = unitRepository.findById(dto.ownerUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found: " + dto.ownerUnitId()));
            objective.setOwnerUnit(unit);
        }

        return objectiveMapper.toDto(objectiveRepository.save(objective));
    }

    @Transactional
    public CompanyObjectiveDto update(Long id, CompanyObjectiveUpdateDto dto) {
        CompanyObjective objective = objectiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Objective not found: " + id));
        objectiveMapper.updateEntity(dto, objective);

        if (dto.ownerUnitId() != null) {
            Unit unit = unitRepository.findById(dto.ownerUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found: " + dto.ownerUnitId()));
            objective.setOwnerUnit(unit);
        } else {
            objective.setOwnerUnit(null);
        }

        return objectiveMapper.toDto(objectiveRepository.save(objective));
    }

    @Transactional
    public void delete(Long id) {
        if (!objectiveRepository.existsById(id)) {
            throw new RuntimeException("Objective not found: " + id);
        }
        objectiveRepository.deleteById(id);
    }
}