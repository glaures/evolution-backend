package expondo.evolution.user;

import expondo.evolution.user.dto.TeamCreateDto;
import expondo.evolution.user.dto.TeamDto;
import expondo.evolution.user.dto.TeamUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<TeamDto> findAll() {
        return teamService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TeamDto create(@RequestBody TeamCreateDto dto) {
        return teamService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamDto update(@PathVariable Long id, @RequestBody TeamUpdateDto dto) {
        return teamService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        teamService.delete(id);
    }
}
