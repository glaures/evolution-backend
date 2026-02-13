package expondo.evolution.okr.dto;

public record TacticDto(
        Long id,
        String code,
        String title,
        String description,
        Integer priority,
        int baseValue,
        Long companyObjectiveId,
        Long responsibleUnitId,
        String responsibleUnitName
) {}