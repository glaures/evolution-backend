package expondo.evolution.okr.dto;

import java.util.List;

/**
 * Response of the reorder endpoint, carrying the new tactic order plus
 * the result of the JIRA push that was triggered as part of the save.
 */
public record ReorderResultDto(
        List<TacticDto> tactics,
        int pushSucceeded,
        int pushFailed,
        boolean jiraConfigured
) {}