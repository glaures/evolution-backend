package expondo.evolution.okr.dto;

/**
 * Lightweight DTO for Key Results, used as reference data in timebox reporting.
 * Includes the parent objective context for grouping in the UI.
 */
public record KeyResultReferenceDto(
        Long id,
        String code,
        String name,
        Long companyObjectiveId,
        String companyObjectiveName
) {}