package expondo.evolution.jira;

public record JiraIssueDto(
        String key,
        String summary,
        String description,
        String objectiveName,
        /**
         * Departments as a comma-separated string (e.g. "Finance, Operations").
         * Empty string if no departments set in JIRA.
         */
        String departments
) {}