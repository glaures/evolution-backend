package expondo.evolution.okr;

import expondo.evolution.okr.dto.CycleCreateDto;
import expondo.evolution.okr.dto.CycleDto;
import expondo.evolution.okr.dto.CycleUpdateDto;
import expondo.evolution.okr.mapper.CycleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CycleService {

    private final CycleRepository cycleRepository;
    private final CycleMapper cycleMapper;

    public List<CycleDto> findAll() {
        return cycleRepository.findAll().stream()
                .map(cycleMapper::toDto)
                .toList();
    }

    public CycleDto findById(Long id) {
        return cycleRepository.findById(id)
                .map(cycleMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Cycle not found: " + id));
    }

    @Transactional
    public CycleDto create(CycleCreateDto dto) {
        Cycle cycle = cycleMapper.toEntity(dto);
        return cycleMapper.toDto(cycleRepository.save(cycle));
    }

    @Transactional
    public CycleDto update(Long id, CycleUpdateDto dto) {
        Cycle cycle = cycleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cycle not found: " + id));
        cycleMapper.updateEntity(dto, cycle);
        return cycleMapper.toDto(cycleRepository.save(cycle));
    }

    @Transactional
    public void delete(Long id) {
        if (!cycleRepository.existsById(id)) {
            throw new RuntimeException("Cycle not found: " + id);
        }
        cycleRepository.deleteById(id);
    }

    @Transactional
    public CycleDto toggleCurrent(Long id) {
        Cycle cycle = cycleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cycle not found: " + id));

        if (cycle.isCurrent()) {
            // Unset current
            cycle.setCurrent(false);
        } else {
            // Unset any existing current cycle
            cycleRepository.findByCurrent(true)
                    .ifPresent(c -> {
                        c.setCurrent(false);
                        cycleRepository.save(c);
                    });
            cycle.setCurrent(true);
        }

        return cycleMapper.toDto(cycleRepository.save(cycle));
    }

    @Transactional
    public CycleDto duplicate(Long id) {
        Cycle original = cycleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cycle not found: " + id));

        Cycle copy = new Cycle();
        copy.setName(original.getName() + " (Copy)");
        copy.setStartDate(original.getStartDate());
        copy.setEndDate(original.getEndDate());
        copy.setCurrent(false);

        return cycleMapper.toDto(cycleRepository.save(copy));
    }
}