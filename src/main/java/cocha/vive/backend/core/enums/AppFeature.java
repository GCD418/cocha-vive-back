package cocha.vive.backend.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppFeature {
    VIEW_UPCOMING_EVENTS("view-upcoming-events"),
    VIEW_FEATURED_EVENTS("view-featured-events"),
    MANAGE_PUBLISHER_REQUESTS("manage-publisher-requests"),
    SEND_WELCOME_EMAIL("send-welcome-email"),
    SEND_NEW_EVENT_NOTIFICATION_EMAIL("send-new-event-notification-email"),
    SEND_NEW_PUBLISHER_REQUEST_NOTIFICATION_EMAIL("send-new-publisher-request-notification-email"),
    NOTIFY_TO_USER_OF_PUBLISHER_REQUEST_CHANGES("notify-to-user-of-publisher-request-changes"),
    FACEBOOK_LOGIN("Facebook-login");

    private final String unleashKey;

}
