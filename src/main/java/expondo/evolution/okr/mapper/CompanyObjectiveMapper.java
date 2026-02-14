package expondo.evolution.okr.mapper;

import expondo.evolution.okr.CompanyObjective;
import expondo.evolution.okr.dto.CompanyObjectiveCreateDto;
import expondo.evolution.okr.dto.CompanyObjectiveDto;
import expondo.evolution.okr.dto.CompanyObjectiveUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {KeyResultMapper.class, TacticMapper.class})
public interface CompanyObjectiveMapper {

    @Mapping(target = "cycleId", source = "cycle.id")
    CompanyObjectiveDto toDto(CompanyObjective entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "cycle", ignore = true)
    @Mapping(target = "keyResults", ignore = true)
    @Mapping(target = "tactics", ignore = true)
    CompanyObjective toEntity(CompanyObjectiveCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cycle", ignore = true)
    @Mapping(target = "keyResults", ignore = true)
    @Mapping(target = "tactics", ignore = true)
    void updateEntity(CompanyObjectiveUpdateDto dto, @MappingTarget CompanyObjective entity);
}