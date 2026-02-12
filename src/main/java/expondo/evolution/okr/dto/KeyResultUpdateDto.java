package expondo.evolution.okr.dto;

public record KeyResultUpdateDto(
        String code,
        String name,
        String description,
        Integer priority
) {}