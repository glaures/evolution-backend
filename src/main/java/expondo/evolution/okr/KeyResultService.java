package expondo.evolution.okr;

import expondo.evolution.okr.dto.KeyResultCreateDto;
import expondo.evolution.okr.dto.KeyResultDto;
import expondo.evolution.okr.dto.KeyResultUpdateDto;
import expondo.evolution.okr.mapper.KeyResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyResultService {

    private final KeyResultRepository keyResultRepository;
    private final CompanyObjectiveRepository objectiveRepository;
    private final KeyResultMapper keyResultMapper;

    public List<KeyResultDto> findByObjectiveId(Long objectiveId) {
        return keyResultRepository.findByCompanyObjectiveIdOrderByPriorityAsc(objectiveId).stream()
                .map(keyResultMapper::toDto)
                .toList();
    }

    public KeyResultDto findById(Long id) {
        return keyResultRepository.findById(id)
                .map(keyResultMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Key Result not found: " + id));
    }

    @Transactional
    public KeyResultDto create(Long objectiveId, KeyResultCreateDto dto) {
        CompanyObjective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new RuntimeException("Objective not found: " + objectiveId));

        KeyResult keyResult = keyResultMapper.toEntity(dto);
        keyResult.setCompanyObjective(objective);
        return keyResultMapper.toDto(keyResultRepository.save(keyResult));
    }

    @Transactional
    public KeyResultDto update(Long id, KeyResultUpdateDto dto) {
        KeyResult keyResult = keyResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Key Result not found: " + id));
        keyResultMapper.updateEntity(dto, keyResult);
        return keyResultMapper.toDto(keyResultRepository.save(keyResult));
    }

    @Transactional
    public void delete(Long id) {
        if (!keyResultRepository.existsById(id)) {
            throw new RuntimeException("Key Result not found: " + id);
        }
        keyResultRepository.deleteById(id);
    }
}