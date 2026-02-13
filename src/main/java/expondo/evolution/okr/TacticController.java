package expondo.evolution.okr;

import expondo.evolution.okr.dto.TacticCreateDto;
import expondo.evolution.okr.dto.TacticDto;
import expondo.evolution.okr.dto.TacticUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
public class TacticController {

    private final TacticService tacticService;

    @GetMapping("/api/objectives/{objectiveId}/tactics")
    public List<TacticDto> findByObjective(@PathVariable Long objectiveId) {
        return tacticService.findByObjectiveId(objectiveId);
    }

    @GetMapping("/api/objectives/{objectiveId}/tactics/{id}")
    public TacticDto findById(@PathVariable Long id) {
        return tacticService.findById(id);
    }

    @PostMapping("/api/objectives/{objectiveId}/tactics")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TacticDto create(@PathVariable Long objectiveId,
                            @RequestBody TacticCreateDto dto) {
        return tacticService.create(objectiveId, dto);
    }

    @PutMapping("/api/objectives/{objectiveId}/tactics/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TacticDto update(@PathVariable Long id,
                            @RequestBody TacticUpdateDto dto) {
        return tacticService.update(id, dto);
    }

    @DeleteMapping("/api/objectives/{objectiveId}/tactics/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        tacticService.delete(id);
    }

    @GetMapping("/api/cycles/{cycleId}/tactics")
    public List<TacticDto> findByCycle(@PathVariable Long cycleId) {
        return tacticService.findByCycleId(cycleId);
    }

    @PutMapping("/api/cycles/{cycleId}/tactics/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TacticDto> reorder(@PathVariable Long cycleId, @RequestBody List<Long> tacticIds) {
        return tacticService.reorder(cycleId, tacticIds);
    }
}