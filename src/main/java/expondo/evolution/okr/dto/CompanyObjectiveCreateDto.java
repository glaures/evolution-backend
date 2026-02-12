package expondo.evolution.okr.dto;

public record CompanyObjectiveCreateDto(
        String code,
        String name,
        String description,
        Long ownerUnitId
) {}