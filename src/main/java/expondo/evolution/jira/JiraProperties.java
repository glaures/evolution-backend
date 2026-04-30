package expondo.evolution.jira;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jira")
public record JiraProperties(
        String baseUrl,
        String userEmail,
        String apiToken,
        Sync sync
) {
    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank()
                && userEmail != null && !userEmail.isBlank()
                && apiToken != null && !apiToken.isBlank();
    }

    public record Sync(
            String projectKey,
            String issueType,
            String fieldObjective,
            String fieldDepartments,
            String fieldCompanyPrio,
            Integer pushQuietPeriodSeconds
    ) {
        public int pushQuietPeriodSecondsOrDefault() {
            return pushQuietPeriodSeconds == null ? 120 : pushQuietPeriodSeconds;
        }
    }
}