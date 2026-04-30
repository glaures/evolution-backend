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
        String activityStatus,
        /**
         * JIRA issue key (e.g. "EXP-1175"), or null for tactics not linked to JIRA.
         */
        String jiraIssueKey,
        /**
         * Departments from JIRA, comma-separated (e.g. "Finance, Operations").
         * Null/blank if the tactic isn't synced from JIRA or has no departments set.
         */
        String jiraDepartments,
        /**
         * Pre-built link to the JIRA issue, e.g. "https://expondo.atlassian.net/browse/EXP-1175".
         * Null if the tactic isn't synced from JIRA.
         */
        String jiraUrl
) {}