package expondo.evolution.okr.dto;

public record KeyResultDto(
        Long id,
        String code,
        String name,
        String description,
        String notes,
        boolean achieved,
        boolean impacted,
        Long companyObjectiveId
) {}