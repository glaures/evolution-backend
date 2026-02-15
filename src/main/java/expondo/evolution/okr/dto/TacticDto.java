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
        String responsibleUnitName,
        /**
         * Activity indicator based on recent effort reporting.
         * ACTIVE = effort in the latest timebox
         * RECENT = effort in the previous timebox but not the latest
         * DORMANT = effort reported at some point, but not recently
         * null = no effort ever reported
         */
        String activityStatus
) {}