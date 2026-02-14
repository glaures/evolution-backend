package expondo.evolution.user.mapper;

import expondo.evolution.user.Team;
import expondo.evolution.user.dto.TeamCreateDto;
import expondo.evolution.user.dto.TeamDto;
import expondo.evolution.user.dto.TeamUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    TeamDto toDto(Team entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timeboxReports", ignore = true)
    Team toEntity(TeamCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timeboxReports", ignore = true)
    void updateEntity(TeamUpdateDto dto, @MappingTarget Team entity);
}
