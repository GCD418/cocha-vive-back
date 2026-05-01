package cocha.vive.backend.core.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public class CacheEnvPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String ttl = environment.getProperty("CACHE_TTL_MINUTES", "30");
        System.setProperty("EHCACHE_TTL_MINUTES", ttl);
    }
}
