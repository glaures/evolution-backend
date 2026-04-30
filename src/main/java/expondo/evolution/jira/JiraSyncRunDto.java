package expondo.evolution.jira;

import java.time.LocalDateTime;

public record JiraSyncRunDto(
        Long id,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        String status,
        int issuesFetched,
        int tacticsCreated,
        int tacticsUpdated,
        int tacticsReactivated,
        int tacticsArchived,
        int tacticsArchivedObjectiveMoved,
        int issuesSkippedNoObjective,
        int issuesSkippedUnknownObjective,
        String message
) {
    public static JiraSyncRunDto from(JiraSyncRun r) {
        return new JiraSyncRunDto(
                r.getId(), r.getStartedAt(), r.getFinishedAt(), r.getStatus(),
                r.getIssuesFetched(), r.getTacticsCreated(), r.getTacticsUpdated(),
                r.getTacticsReactivated(), r.getTacticsArchived(),
                r.getTacticsArchivedObjectiveMoved(),
                r.getIssuesSkippedNoObjective(), r.getIssuesSkippedUnknownObjective(),
                r.getMessage()
        );
    }
}