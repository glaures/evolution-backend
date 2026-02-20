package expondo.evolution.okr;

import expondo.evolution.okr.dto.CycleCreateDto;
import expondo.evolution.okr.dto.CycleDto;
import expondo.evolution.okr.dto.CycleUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cycles")
@RequiredArgsConstructor
public class CycleController {

    private final CycleService cycleService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<CycleDto> findAll() {
        return cycleService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public CycleDto findById(@PathVariable Long id) {
        return cycleService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CycleDto create(@RequestBody CycleCreateDto dto) {
        return cycleService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CycleDto update(@PathVariable Long id, @RequestBody CycleUpdateDto dto) {
        return cycleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        cycleService.delete(id);
    }

    @PostMapping("/{id}/toggle-current")
    @PreAuthorize("hasRole('ADMIN')")
    public CycleDto toggleCurrent(@PathVariable Long id) {
        return cycleService.toggleCurrent(id);
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasRole('ADMIN')")
    public CycleDto duplicate(@PathVariable Long id) {
        return cycleService.duplicate(id);
    }
}