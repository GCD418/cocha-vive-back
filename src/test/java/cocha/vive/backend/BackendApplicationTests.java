package cocha.vive.backend;

import io.getunleash.Unleash;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestFeatureToggleConfig {
        @Bean("unleash")
        @Primary
        Unleash unleash() {
            return mock(Unleash.class);
        }
    }

}
