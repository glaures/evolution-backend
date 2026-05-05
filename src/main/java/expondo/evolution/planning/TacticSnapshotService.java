package expondo.evolution.planning;

import expondo.evolution.jira.JiraProperties;
import expondo.evolution.planning.dto.TacticSnapshotDto;
import expondo.evolution.planning.dto.TimeboxSnapshotSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TacticSnapshotService {

    private final TimeboxTacticSnapshotRepository snapshotRepository;
    private final TimeboxRepository timeboxRepository;
    private final JiraProperties jiraProperties;

    /**
     * Returns the list of timeboxes in a cycle that have at least one snapshot,
     * ordered by timebox number. Useful for populating a "view" dropdown.
     */
    @Transactional(readOnly = true)
    public List<TimeboxSnapshotSummaryDto> findTimeboxesWithSnapshots(Long cycleId) {
        List<Long> ids = snapshotRepository.findTimeboxIdsWithSnapshotsByCycleId(cycleId);
        if (ids.isEmpty()) return List.of();
        return timeboxRepository.findAllById(ids).stream()
                .sorted((a, b) -> Integer.compare(a.getNumber(), b.getNumber()))
                .map(tb -> new TimeboxSnapshotSummaryDto(
                        tb.getId(), tb.getNumber(), tb.getStartDate(), tb.getEndDate(), tb.isClosed()
                ))
                .toList();
    }

    /**
     * Returns the snapshot for a given timebox: tactics ordered by their priority
     * at the moment the timebox opened, including archived ones (so the historical
     * order remains intact). Tactics created after the timebox opened are not included.
     */
    @Transactional(readOnly = true)
    public List<TacticSnapshotDto> findSnapshot(Long timeboxId) {
        return snapshotRepository.findByTimeboxIdWithTactic(timeboxId).stream()
                .map(s -> new TacticSnapshotDto(
                        s.getTactic().getId(),
                        s.getTactic().getCode(),
                        s.getTactic().getTitle(),
                        s.getPriorityAtOpen(),
                        s.getScoreAtOpen(),
                        s.getTactic().getCompanyObjective().getId(),
                        s.getTactic().getCompanyObjective().getCode(),
                        s.getTactic().isArchived(),
                        s.getTactic().getJiraIssueKey(),
                        buildJiraUrl(s.getTactic().getJiraIssueKey()),
                        s.getTactic().getJiraDepartments()
                ))
                .toList();
    }

    private String buildJiraUrl(String issueKey) {
        if (issueKey == null || issueKey.isBlank()) return null;
        if (jiraProperties.baseUrl() == null || jiraProperties.baseUrl().isBlank()) return null;
        String base = jiraProperties.baseUrl();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/browse/" + issueKey;
    }
}