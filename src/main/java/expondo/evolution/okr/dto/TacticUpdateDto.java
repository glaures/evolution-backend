package expondo.evolution.okr.dto;

public record TacticUpdateDto(
        String code,
        String title,
        String description,
        Integer priority,
        Long responsibleUnitId,
        Long companyObjectiveId
) {}