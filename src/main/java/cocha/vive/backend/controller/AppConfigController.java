package cocha.vive.backend.controller;

import cocha.vive.backend.core.enums.AppFeature;
import cocha.vive.backend.service.FeatureToggleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "App Config", description = "Application feature-flag configuration")
public class AppConfigController {
    private final FeatureToggleService featureToggleService;

    @Operation(
        summary = "Get all feature flags",
        description = "Returns a map of feature-flag names to their current enabled/disabled state. " +
                      "Returns an empty map when no feature flags are configured."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Feature flags retrieved successfully")
    })
    @SecurityRequirements   // publicly accessible
    @GetMapping("/features")
    public Map<String, Boolean> getFeatures() {
        return Arrays.stream(AppFeature.values())
            .collect(Collectors.toMap(
                feature -> feature.name().toLowerCase(),
                feature -> featureToggleService.isEnabled(feature.getUnleashKey())
            ));
    }
}
