package expondo.evolution.okr.mapper;

import expondo.evolution.okr.CompanyObjective;
import expondo.evolution.okr.dto.CompanyObjectiveCreateDto;
import expondo.evolution.okr.dto.CompanyObjectiveDto;
import expondo.evolution.okr.dto.CompanyObjectiveUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = KeyResultMapper.class)
public interface CompanyObjectiveMapper {

    @Mapping(target = "cycleId", source = "cycle.id")
    @Mapping(target = "ownerUnitId", source = "ownerUnit.id")
    @Mapping(target = "ownerUnitName", source = "ownerUnit.name")
    CompanyObjectiveDto toDto(CompanyObjective entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cycle", ignore = true)
    @Mapping(target = "ownerUnit", ignore = true)
    @Mapping(target = "keyResults", ignore = true)
    CompanyObjective toEntity(CompanyObjectiveCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cycle", ignore = true)
    @Mapping(target = "ownerUnit", ignore = true)
    @Mapping(target = "keyResults", ignore = true)
    void updateEntity(CompanyObjectiveUpdateDto dto, @MappingTarget CompanyObjective entity);
}