package cocha.vive.backend.config;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {
    @Value("${unleash.app-name}")
    private String appName;

    @Value("${unleash.api-url}")
    private String apiUrl;

    @Value("${unleash.api-token}")
    private String apiToken;

    @Value("${unleash.environment}")
    private String environment;

    @Bean
    public Unleash unleash() {
        UnleashConfig config = UnleashConfig.builder()
            .appName(appName)
            .instanceId("backend-" + environment)
            .unleashAPI(apiUrl)
            .apiKey(apiToken)
            .synchronousFetchOnInitialisation(true)
            .fetchTogglesInterval(15)
            .build();

        return new DefaultUnleash(config);
    }
}
