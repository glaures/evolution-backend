package expondo.evolution.okr;

import expondo.evolution.okr.dto.TacticActivityDto;
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
    @PreAuthorize("hasRole('USER')")
    public List<TacticDto> findByObjective(@PathVariable Long objectiveId) {
        return tacticService.findByObjectiveId(objectiveId);
    }

    @GetMapping("/api/objectives/{objectiveId}/tactics/{id}")
    @PreAuthorize("hasRole('USER')")
    public TacticDto findById(@PathVariable Long id) {
        return tacticService.findById(id);
    }

    @PostMapping("/api/objectives/{objectiveId}/tactics")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public TacticDto create(@PathVariable Long objectiveId,
                            @RequestBody TacticCreateDto dto) {
        return tacticService.create(objectiveId, dto);
    }

    @PutMapping("/api/objectives/{objectiveId}/tactics/{id}")
    @PreAuthorize("hasRole('USER')")
    public TacticDto update(@PathVariable Long id,
                            @RequestBody TacticUpdateDto dto) {
        return tacticService.update(id, dto);
    }

    /**
     * Soft-delete (archive). Always succeeds.
     */
    @PostMapping("/api/objectives/{objectiveId}/tactics/{id}/archive")
    @PreAuthorize("hasRole('USER')")
    public TacticDto archive(@PathVariable Long id) {
        return tacticService.archive(id);
    }

    /**
     * Hard delete. Throws if the tactic has historical data — clients should
     * fall back to /archive in that case.
     */
    @DeleteMapping("/api/objectives/{objectiveId}/tactics/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable Long id) {
        tacticService.delete(id);
    }

    @GetMapping("/api/cycles/{cycleId}/tactics")
    @PreAuthorize("hasRole('USER')")
    public List<TacticDto> findByCycle(@PathVariable Long cycleId) {
        return tacticService.findByCycleId(cycleId);
    }

    @PutMapping("/api/cycles/{cycleId}/tactics/reorder")
    @PreAuthorize("hasRole('USER')")
    public List<TacticDto> reorder(@PathVariable Long cycleId, @RequestBody List<Long> tacticIds) {
        return tacticService.reorder(cycleId, tacticIds);
    }

    @GetMapping("/api/tactics/{tacticId}/activity")
    @PreAuthorize("hasRole('USER')")
    public TacticActivityDto getActivity(@PathVariable Long tacticId) {
        return tacticService.getActivity(tacticId);
    }
}
