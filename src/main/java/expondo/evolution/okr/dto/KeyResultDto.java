package expondo.evolution.okr.dto;

public record KeyResultDto(
        Long id,
        String code,
        String name,
        String description,
        Integer priority,
        int baseValue,
        Long companyObjectiveId
) {}