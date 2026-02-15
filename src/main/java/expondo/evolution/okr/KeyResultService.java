package expondo.evolution.okr;

import expondo.evolution.okr.dto.KeyResultDto;
import expondo.evolution.okr.dto.KeyResultReferenceDto;
import expondo.evolution.okr.dto.KeyResultSaveDto;
import expondo.evolution.okr.mapper.KeyResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyResultService {

    private final KeyResultRepository keyResultRepository;
    private final KeyResultMapper keyResultMapper;

    /**
     * Get all Key Results for a cycle as lightweight reference data.
     * Used by the timebox report form to populate the KR impact selector.
     */
    public List<KeyResultReferenceDto> findReferenceByCycleId(Long cycleId) {
        return keyResultRepository.findByCycleId(cycleId).stream()
                .map(kr -> new KeyResultReferenceDto(
                        kr.getId(),
                        kr.getCode(),
                        kr.getName(),
                        kr.getCompanyObjective().getId(),
                        kr.getCompanyObjective().getName()
                ))
                .toList();
    }

    @Transactional
    public KeyResultDto create(CompanyObjective objective, KeyResultSaveDto dto) {
        KeyResult kr = new KeyResult();
        kr.setCompanyObjective(objective);
        kr.setCode(generateCode(objective));
        kr.setName(dto.name());
        kr.setDescription(dto.description());
        kr.setNotes(dto.notes());
        return keyResultMapper.toDto(keyResultRepository.save(kr));
    }

    @Transactional
    public KeyResultDto update(Long id, KeyResultSaveDto dto) {
        KeyResult kr = keyResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KeyResult not found: " + id));
        kr.setName(dto.name());
        kr.setDescription(dto.description());
        kr.setNotes(dto.notes());
        return keyResultMapper.toDto(keyResultRepository.save(kr));
    }

    @Transactional
    public void delete(Long id) {
        if (!keyResultRepository.existsById(id)) {
            throw new RuntimeException("KeyResult not found: " + id);
        }
        keyResultRepository.deleteById(id);
    }

    private String generateCode(CompanyObjective objective) {
        String objectiveNum = objective.getCode().replaceAll("[^0-9]", "");
        long count = keyResultRepository.findByCompanyObjectiveId(objective.getId()).size();
        return "KR" + objectiveNum + "." + (count + 1);
    }
}