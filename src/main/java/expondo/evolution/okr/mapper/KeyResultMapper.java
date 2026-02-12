package expondo.evolution.okr.mapper;

import expondo.evolution.okr.KeyResult;
import expondo.evolution.okr.dto.KeyResultCreateDto;
import expondo.evolution.okr.dto.KeyResultDto;
import expondo.evolution.okr.dto.KeyResultUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface KeyResultMapper {

    @Mapping(target = "companyObjectiveId", source = "companyObjective.id")
    @Mapping(target = "baseValue", expression = "java(entity.getBaseValue())")
    KeyResultDto toDto(KeyResult entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyObjective", ignore = true)
    @Mapping(target = "deliverables", ignore = true)
    KeyResult toEntity(KeyResultCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyObjective", ignore = true)
    @Mapping(target = "deliverables", ignore = true)
    void updateEntity(KeyResultUpdateDto dto, @MappingTarget KeyResult entity);
}