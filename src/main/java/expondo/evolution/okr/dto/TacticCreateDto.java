package expondo.evolution.okr.dto;

public record TacticCreateDto(
        String title,
        String description,
        Integer priority,
        Long responsibleUnitId
) {}