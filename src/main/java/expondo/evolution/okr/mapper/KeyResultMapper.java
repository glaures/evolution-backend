package expondo.evolution.okr.mapper;

import expondo.evolution.okr.KeyResult;
import expondo.evolution.okr.dto.KeyResultDto;
import expondo.evolution.planning.DeliveryKeyResultImpact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KeyResultMapper {

    /**
     * MapStruct cannot derive 'achieved' and 'impacted' automatically since they
     * are computed from the deliveryImpacts collection. We use a default method
     * to handle this, delegating simple field mapping to a named internal method.
     */
    default KeyResultDto toDto(KeyResult entity) {
        if (entity == null) return null;

        boolean achieved = entity.getDeliveryImpacts() != null &&
                entity.getDeliveryImpacts().stream()
                        .anyMatch(i -> i.getImpactType() == DeliveryKeyResultImpact.ImpactType.ACHIEVES);

        boolean impacted = entity.getDeliveryImpacts() != null &&
                !entity.getDeliveryImpacts().isEmpty();

        return new KeyResultDto(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getNotes(),
                achieved,
                impacted,
                entity.getCompanyObjective().getId()
        );
    }
}