package expondo.evolution.okr;

import expondo.evolution.okr.dto.KeyResultCreateDto;
import expondo.evolution.okr.dto.KeyResultDto;
import expondo.evolution.okr.dto.KeyResultUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/objectives/{objectiveId}/key-results")
@RequiredArgsConstructor
public class KeyResultController {

    private final KeyResultService keyResultService;

    @GetMapping
    public List<KeyResultDto> findByObjective(@PathVariable Long objectiveId) {
        return keyResultService.findByObjectiveId(objectiveId);
    }

    @GetMapping("/{id}")
    public KeyResultDto findById(@PathVariable Long id) {
        return keyResultService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public KeyResultDto create(@PathVariable Long objectiveId,
                               @RequestBody KeyResultCreateDto dto) {
        return keyResultService.create(objectiveId, dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public KeyResultDto update(@PathVariable Long id,
                               @RequestBody KeyResultUpdateDto dto) {
        return keyResultService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        keyResultService.delete(id);
    }
}