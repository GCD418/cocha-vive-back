package cocha.vive.backend.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppFeature {
    VIEW_UPCOMING_EVENTS("view-upcoming-events"),
    VIEW_FEATURED_EVENTS("view-featured-events");

    private final String unleashKey;

}
