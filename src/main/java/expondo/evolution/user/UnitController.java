package expondo.evolution.user;

import expondo.evolution.user.dto.UnitCreateDto;
import expondo.evolution.user.dto.UnitDto;
import expondo.evolution.user.dto.UnitUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    public List<UnitDto> findAll() {
        return unitService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UnitDto create(@RequestBody UnitCreateDto dto) {
        return unitService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UnitDto update(@PathVariable Long id, @RequestBody UnitUpdateDto dto) {
        return unitService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        unitService.delete(id);
    }
}