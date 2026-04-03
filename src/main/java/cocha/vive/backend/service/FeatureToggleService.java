package cocha.vive.backend.service;

import io.getunleash.Unleash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeatureToggleService {
    private final Unleash unleash;

    public boolean isEnabled(String flagName) {
        return unleash.isEnabled(flagName);
    }
}
