package expondo.evolution.planning.dto;

public record TacticSnapshotDto(
        Long tacticId,
        String code,
        String title,
        Integer priorityAtOpen,
        Integer scoreAtOpen,
        Long companyObjectiveId,
        String companyObjectiveCode,
        boolean archived,
        String jiraIssueKey,
        String jiraUrl,
        /**
         * Departments come from the live Tactic — there's no historical
         * snapshot of this field. The current value is shown for filtering
         * convenience in the snapshot view.
         */
        String jiraDepartments
) {}