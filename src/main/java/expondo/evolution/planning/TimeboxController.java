package expondo.evolution.planning;

import expondo.evolution.planning.dto.TimeboxDto;
import expondo.evolution.planning.dto.TimeboxGenerateSeriesDto;
import expondo.evolution.planning.dto.TimeboxUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cycles/{cycleId}/timeboxes")
@RequiredArgsConstructor
public class TimeboxController {

    private final TimeboxService timeboxService;

    @GetMapping
    public List<TimeboxDto> findByCycle(@PathVariable Long cycleId) {
        return timeboxService.findByCycleId(cycleId);
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public List<TimeboxDto> generateSeries(
            @PathVariable Long cycleId,
            @RequestBody TimeboxGenerateSeriesDto dto) {
        return timeboxService.generateSeries(cycleId, dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TimeboxDto update(@PathVariable Long cycleId, @PathVariable Long id, @RequestBody TimeboxUpdateDto dto) {
        return timeboxService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long cycleId, @PathVariable Long id) {
        timeboxService.delete(id);
    }
}
