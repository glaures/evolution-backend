package expondo.evolution.user.mapper;

import expondo.evolution.user.Unit;
import expondo.evolution.user.dto.UnitCreateDto;
import expondo.evolution.user.dto.UnitDto;
import expondo.evolution.user.dto.UnitUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    UnitDto toDto(Unit entity);

    @Mapping(target = "id", ignore = true)
    Unit toEntity(UnitCreateDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntity(UnitUpdateDto dto, @MappingTarget Unit entity);
}