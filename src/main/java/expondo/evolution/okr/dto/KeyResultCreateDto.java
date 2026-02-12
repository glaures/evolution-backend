package expondo.evolution.okr.dto;

public record KeyResultCreateDto(
        String code,
        String name,
        String description,
        Integer priority
) {}