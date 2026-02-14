package expondo.evolution.planning.mapper;

import expondo.evolution.planning.Timebox;
import expondo.evolution.planning.dto.TimeboxDto;
import expondo.evolution.planning.dto.TimeboxUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TimeboxMapper {

    @Mapping(source = "cycle.id", target = "cycleId")
    TimeboxDto toDto(Timebox entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cycle", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "timeboxReports", ignore = true)
    void updateEntity(TimeboxUpdateDto dto, @MappingTarget Timebox entity);
}
