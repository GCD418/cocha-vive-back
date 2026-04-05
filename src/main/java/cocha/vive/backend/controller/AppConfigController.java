package cocha.vive.backend.controller;

import cocha.vive.backend.core.enums.AppFeature;
import cocha.vive.backend.service.FeatureToggleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/config")
@RequiredArgsConstructor
public class AppConfigController {
    private final FeatureToggleService featureToggleService;

    @GetMapping("/features")
    public Map<String, Boolean> getFeatures() {
        return Arrays.stream(AppFeature.values())
            .collect(Collectors.toMap(
                feature -> feature.name().toLowerCase(),
                feature -> featureToggleService.isEnabled(feature.getUnleashKey())
            ));
    }
}
