package cocha.vive.backend.config;

import io.getunleash.DefaultUnleash;
import io.getunleash.FakeUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    @Bean
    @ConditionalOnProperty(name = "unleash.enabled", havingValue = "true")
    public Unleash unleash(
            @Value("${unleash.app-name}") String appName,
            @Value("${unleash.api-url}") String apiUrl,
            @Value("${unleash.api-token}") String apiToken,
            @Value("${unleash.environment}") String environment
    ) {
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

    @Bean
    @ConditionalOnMissingBean(Unleash.class)
    public Unleash unleashNoOp() {
        return new FakeUnleash();
    }
}
