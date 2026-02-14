package expondo.evolution.okr.dto;

public record TacticDto(
        Long id,
        String code,
        String title,
        String description,
        Integer priority,
        int score,
        Long companyObjectiveId,
        Long responsibleUnitId,
        String responsibleUnitName
) {}