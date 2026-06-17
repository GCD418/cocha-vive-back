package cocha.vive.backend.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppFeature {
    SEND_NEW_EVENT_NOTIFICATION_EMAIL("send-new-event-notification-email");
    private final String unleashKey;

}
