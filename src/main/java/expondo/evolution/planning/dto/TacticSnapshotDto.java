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
        String jiraUrl
) {}
