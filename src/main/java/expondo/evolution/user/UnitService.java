package expondo.evolution.user;

import expondo.evolution.user.dto.UnitCreateDto;
import expondo.evolution.user.dto.UnitDto;
import expondo.evolution.user.dto.UnitUpdateDto;
import expondo.evolution.user.mapper.UnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;

    public List<UnitDto> findAll() {
        return unitRepository.findAll().stream()
                .map(unitMapper::toDto)
                .toList();
    }

    @Transactional
    public UnitDto create(UnitCreateDto dto) {
        return unitMapper.toDto(unitRepository.save(unitMapper.toEntity(dto)));
    }

    @Transactional
    public UnitDto update(Long id, UnitUpdateDto dto) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found: " + id));
        unitMapper.updateEntity(dto, unit);
        return unitMapper.toDto(unitRepository.save(unit));
    }

    @Transactional
    public void delete(Long id) {
        if (!unitRepository.existsById(id)) {
            throw new RuntimeException("Unit not found: " + id);
        }
        unitRepository.deleteById(id);
    }
}