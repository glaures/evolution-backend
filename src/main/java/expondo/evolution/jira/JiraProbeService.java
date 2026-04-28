package expondo.evolution.jira;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Smoke-test service for JIRA Cloud connectivity. Not used by anything else yet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JiraProbeService {

    private final RestClient jiraRestClient;
    private final JiraProperties props;

    /**
     * Calls GET /rest/api/3/myself — minimal call that verifies network reachability,
     * TLS, authentication and JSON parsing in one shot.
     */
    public Map<String, Object> whoami() {
        ensureConfigured();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = jiraRestClient.get()
                    .uri("/rest/api/3/myself")
                    .retrieve()
                    .body(Map.class);
            log.info("JIRA whoami succeeded: accountId={}, displayName={}, emailAddress={}",
                    body == null ? null : body.get("accountId"),
                    body == null ? null : body.get("displayName"),
                    body == null ? null : body.get("emailAddress"));
            return body;
        } catch (RestClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            log.error("JIRA whoami failed: status={}, body={}", status, e.getResponseBodyAsString());
            throw new JiraProbeException("JIRA whoami failed with status " + status, e);
        } catch (Exception e) {
            log.error("JIRA whoami failed (network/TLS/other): {}", e.getMessage(), e);
            throw new JiraProbeException("JIRA whoami failed: " + e.getMessage(), e);
        }
    }

    /**
     * Calls GET /rest/api/3/issue/{key} — verifies that the authenticated user
     * can actually read project data.
     */
    public Map<String, Object> getIssue(String issueKey) {
        ensureConfigured();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = jiraRestClient.get()
                    .uri("/rest/api/3/issue/{key}", issueKey)
                    .retrieve()
                    .body(Map.class);
            log.info("JIRA getIssue({}) succeeded", issueKey);
            return body;
        } catch (RestClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            log.error("JIRA getIssue({}) failed: status={}, body={}", issueKey, status, e.getResponseBodyAsString());
            throw new JiraProbeException("JIRA getIssue failed with status " + status, e);
        } catch (Exception e) {
            log.error("JIRA getIssue({}) failed: {}", issueKey, e.getMessage(), e);
            throw new JiraProbeException("JIRA getIssue failed: " + e.getMessage(), e);
        }
    }

    private void ensureConfigured() {
        if (!props.isConfigured()) {
            throw new JiraProbeException(
                    "JIRA is not configured. Set JIRA_BASE_URL, JIRA_USER_EMAIL, JIRA_API_TOKEN."
            );
        }
    }

    public static class JiraProbeException extends RuntimeException {
        public JiraProbeException(String message) { super(message); }
        public JiraProbeException(String message, Throwable cause) { super(message, cause); }
    }
}
