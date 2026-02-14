package expondo.evolution.user;

import expondo.evolution.user.dto.TeamCreateDto;
import expondo.evolution.user.dto.TeamDto;
import expondo.evolution.user.dto.TeamUpdateDto;
import expondo.evolution.user.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    public List<TeamDto> findAll() {
        return teamRepository.findAll().stream()
                .map(teamMapper::toDto)
                .toList();
    }

    @Transactional
    public TeamDto create(TeamCreateDto dto) {
        return teamMapper.toDto(teamRepository.save(teamMapper.toEntity(dto)));
    }

    @Transactional
    public TeamDto update(Long id, TeamUpdateDto dto) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found: " + id));
        teamMapper.updateEntity(dto, team);
        return teamMapper.toDto(teamRepository.save(team));
    }

    @Transactional
    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new RuntimeException("Team not found: " + id);
        }
        teamRepository.deleteById(id);
    }
}
