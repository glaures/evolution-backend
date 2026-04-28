package expondo.evolution.jira;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the JIRA Cloud REST API client.
 *
 * Bound from environment variables via Spring's relaxed binding:
 *   JIRA_BASE_URL, JIRA_USER_EMAIL, JIRA_API_TOKEN
 *
 * Or, equivalently, from application.yml under the "jira:" prefix.
 */
@ConfigurationProperties(prefix = "jira")
public record JiraProperties(
        String baseUrl,
        String userEmail,
        String apiToken
) {
    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank()
                && userEmail != null && !userEmail.isBlank()
                && apiToken != null && !apiToken.isBlank();
    }
}
