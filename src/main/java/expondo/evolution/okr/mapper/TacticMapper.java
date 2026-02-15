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

    /**
     * Maps a Tactic entity to a DTO.
     * activityStatus is set to null here since it requires cycle-level context.
     * Use toDtoWithActivity() for enriched mapping.
     */
    default TacticDto toDto(Tactic entity) {
        return toDtoWithActivity(entity, null);
    }

    /**
     * Maps a Tactic entity to a DTO with an explicit activity status.
     */
    default TacticDto toDtoWithActivity(Tactic entity, String activityStatus) {
        if (entity == null) return null;
        return new TacticDto(
                entity.getId(),
                entity.getCode(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPriority(),
                entity.getScore(),
                entity.getCompanyObjective().getId(),
                entity.getResponsibleUnit() != null ? entity.getResponsibleUnit().getId() : null,
                entity.getResponsibleUnit() != null ? entity.getResponsibleUnit().getName() : null,
                activityStatus
        );
    }

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