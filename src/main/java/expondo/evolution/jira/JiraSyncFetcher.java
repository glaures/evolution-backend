package expondo.evolution.jira;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pulls Tactic issues from JIRA Cloud using /rest/api/3/search/jql with
 * token-based pagination.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JiraSyncFetcher {

    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 50;

    private final RestClient jiraRestClient;
    private final JiraProperties props;

    public List<JiraIssueDto> fetchAllTactics() {
        if (!props.isConfigured()) {
            throw new IllegalStateException("JIRA is not configured");
        }
        if (props.sync() == null
                || props.sync().projectKey() == null
                || props.sync().issueType() == null
                || props.sync().fieldObjective() == null) {
            throw new IllegalStateException(
                    "JIRA sync is not fully configured (need projectKey, issueType, fieldObjective)");
        }

        String jql = String.format(
                "project = %s AND issuetype = \"%s\" ORDER BY Rank ASC",
                props.sync().projectKey(),
                props.sync().issueType()
        );
        String fieldObjective = props.sync().fieldObjective();
        String fieldDepartments = props.sync().fieldDepartments(); // may be null

        // Build the requested fields list dynamically
        List<String> requestedFields = new ArrayList<>();
        requestedFields.add("summary");
        requestedFields.add("description");
        requestedFields.add(fieldObjective);
        if (fieldDepartments != null && !fieldDepartments.isBlank()) {
            requestedFields.add(fieldDepartments);
        }

        List<JiraIssueDto> result = new ArrayList<>();
        String nextPageToken = null;
        int pages = 0;

        while (pages < MAX_PAGES) {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jql", jql);
            requestBody.put("maxResults", PAGE_SIZE);
            requestBody.put("fields", requestedFields);
            if (nextPageToken != null) {
                requestBody.put("nextPageToken", nextPageToken);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> body = jiraRestClient.post()
                    .uri("/rest/api/3/search/jql")
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (body == null) break;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> issues = (List<Map<String, Object>>) body.get("issues");
            if (issues == null || issues.isEmpty()) break;

            for (Map<String, Object> issue : issues) {
                result.add(parseIssue(issue, fieldObjective, fieldDepartments));
            }

            pages++;
            Object token = body.get("nextPageToken");
            if (token == null) break;
            nextPageToken = token.toString();
        }

        if (pages >= MAX_PAGES) {
            log.warn("JIRA fetch hit page cap of {} (total {} issues fetched)",
                    MAX_PAGES, result.size());
        }

        log.info("Fetched {} tactic(s) from JIRA", result.size());
        return result;
    }

    @SuppressWarnings("unchecked")
    private JiraIssueDto parseIssue(Map<String, Object> issue, String fieldObjective, String fieldDepartments) {
        String key = (String) issue.get("key");
        Map<String, Object> fields = (Map<String, Object>) issue.getOrDefault("fields", Map.of());

        String summary = (String) fields.get("summary");
        String description = AdfToTextConverter.convert(fields.get("description"));

        String objectiveName = extractSingleSelectValue(fields.get(fieldObjective));

        String departments = "";
        if (fieldDepartments != null && !fieldDepartments.isBlank()) {
            departments = extractMultiSelectValues(fields.get(fieldDepartments));
        }

        return new JiraIssueDto(key, summary, description, objectiveName, departments);
    }

    /**
     * Extracts the value from a single-select-style custom field (often returned
     * as a list of one or as a single map).
     */
    @SuppressWarnings("unchecked")
    private static String extractSingleSelectValue(Object field) {
        if (field == null) return null;
        if (field instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> first) {
            Object v = first.get("value");
            return v == null ? null : v.toString();
        }
        if (field instanceof Map<?, ?> single) {
            Object v = single.get("value");
            return v == null ? null : v.toString();
        }
        return null;
    }

    /**
     * Extracts a comma-joined list of values from a multi-select custom field
     * (returned as a list of maps with "value" keys).
     */
    @SuppressWarnings("unchecked")
    private static String extractMultiSelectValues(Object field) {
        if (field == null) return "";
        if (field instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item instanceof Map<?, ?>)
                    .map(item -> ((Map<String, Object>) item).get("value"))
                    .filter(v -> v != null)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
        }
        return "";
    }
}