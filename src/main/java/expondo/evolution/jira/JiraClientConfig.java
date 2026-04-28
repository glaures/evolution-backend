package expondo.evolution.jira;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(JiraProperties.class)
public class JiraClientConfig {

    /**
     * Builds a RestClient pre-configured with the JIRA base URL and Basic Auth header.
     * Returns null-safe behavior if config is missing — callers are expected to
     * check {@link JiraProperties#isConfigured()} before using.
     */
    @Bean
    public RestClient jiraRestClient(JiraProperties props) {
        RestClient.Builder builder = RestClient.builder();
        if (props.isConfigured()) {
            String credentials = props.userEmail() + ":" + props.apiToken();
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            builder
                    .baseUrl(props.baseUrl())
                    .defaultHeader("Authorization", "Basic " + encoded)
                    .defaultHeader("Accept", "application/json");
        }
        return builder.build();
    }
}
