package expondo.evolution.okr;

import expondo.evolution.okr.dto.CompanyObjectiveCreateDto;
import expondo.evolution.okr.dto.CompanyObjectiveDto;
import expondo.evolution.okr.dto.CompanyObjectiveUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cycles/{cycleId}/objectives")
@RequiredArgsConstructor
public class CompanyObjectiveController {

    private final CompanyObjectiveService objectiveService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<CompanyObjectiveDto> findByCycle(@PathVariable Long cycleId) {
        return objectiveService.findByCycleId(cycleId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public CompanyObjectiveDto findById(@PathVariable Long id) {
        return objectiveService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public CompanyObjectiveDto create(@PathVariable Long cycleId,
                                      @RequestBody CompanyObjectiveCreateDto dto) {
        return objectiveService.create(cycleId, dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public CompanyObjectiveDto update(@PathVariable Long id,
                                      @RequestBody CompanyObjectiveUpdateDto dto) {
        return objectiveService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable Long id) {
        objectiveService.delete(id);
    }
}