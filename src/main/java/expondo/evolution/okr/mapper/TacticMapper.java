package expondo.evolution.okr.mapper;

import expondo.evolution.okr.Tactic;
import expondo.evolution.okr.dto.TacticCreateDto;
import expondo.evolution.okr.dto.TacticDto;
import expondo.evolution.okr.dto.TacticUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TacticMapper {

    @Mapping(target = "companyObjectiveId", source = "companyObjective.id")
    @Mapping(target = "responsibleUnitId", source = "responsibleUnit.id")
    @Mapping(target = "responsibleUnitName", source = "responsibleUnit.name")
    @Mapping(target = "score", expression = "java(entity.getScore())")
    TacticDto toDto(Tactic entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "companyObjective", ignore = true)
    @Mapping(target = "responsibleUnit", ignore = true)
    Tactic toEntity(TacticCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyObjective", ignore = true)
    @Mapping(target = "responsibleUnit", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntity(TacticUpdateDto dto, @MappingTarget Tactic entity);
}
