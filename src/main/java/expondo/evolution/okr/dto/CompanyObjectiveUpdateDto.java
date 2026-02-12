package expondo.evolution.okr.dto;

public record CompanyObjectiveUpdateDto(
        String code,
        String name,
        String description,
        Long ownerUnitId
) {}