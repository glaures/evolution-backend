package expondo.evolution.okr;

import expondo.evolution.okr.dto.KeyResultDto;
import expondo.evolution.okr.dto.KeyResultReferenceDto;
import expondo.evolution.okr.dto.KeyResultSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KeyResultController {

    private final KeyResultService keyResultService;
    private final CompanyObjectiveRepository objectiveRepository;

    /**
     * Get all Key Results for a cycle as reference data.
     * Used by timebox report form for the KR impact selector.
     */
    @GetMapping("/cycles/{cycleId}/key-results")
    @PreAuthorize("hasRole('USER')")
    public List<KeyResultReferenceDto> findByCycle(@PathVariable Long cycleId) {
        return keyResultService.findReferenceByCycleId(cycleId);
    }

    /**
     * Create a new Key Result under an objective.
     */
    @PostMapping("/objectives/{objectiveId}/key-results")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public KeyResultDto create(@PathVariable Long objectiveId, @RequestBody KeyResultSaveDto dto) {
        CompanyObjective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new RuntimeException("Objective not found: " + objectiveId));
        return keyResultService.create(objective, dto);
    }

    /**
     * Update a Key Result.
     */
    @PutMapping("/objectives/{objectiveId}/key-results/{id}")
    @PreAuthorize("hasRole('USER')")
    public KeyResultDto update(@PathVariable Long objectiveId, @PathVariable Long id,
                               @RequestBody KeyResultSaveDto dto) {
        return keyResultService.update(id, dto);
    }

    /**
     * Delete a Key Result.
     */
    @DeleteMapping("/objectives/{objectiveId}/key-results/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable Long objectiveId, @PathVariable Long id) {
        keyResultService.delete(id);
    }
}