package cocha.vive.backend.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppFeature {
    VIEW_UPCOMING_EVENTS("view-upcoming-events"),
    SEND_NEW_EVENT_NOTIFICATION_EMAIL("send-new-event-notification-email");
    private final String unleashKey;

}
