package expondo.evolution.okr.mapper;

import expondo.evolution.okr.Cycle;
import expondo.evolution.okr.dto.CycleCreateDto;
import expondo.evolution.okr.dto.CycleDto;
import expondo.evolution.okr.dto.CycleUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CycleMapper {

    @Mapping(target = "objectiveCount", expression = "java(cycle.getCompanyObjectives().size())")
    @Mapping(target = "timeboxCount", expression = "java(cycle.getTimeboxes().size())")
    CycleDto toDto(Cycle cycle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "current", constant = "false")
    @Mapping(target = "companyObjectives", ignore = true)
    @Mapping(target = "timeboxes", ignore = true)
    Cycle toEntity(CycleCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "companyObjectives", ignore = true)
    @Mapping(target = "timeboxes", ignore = true)
    void updateEntity(CycleUpdateDto dto, @MappingTarget Cycle cycle);
}