package cocha.vive.backend.service;

import cocha.vive.backend.config.EventNotificationProperties;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventUserNotificationFacade tests")
class EventUserNotificationFacadeTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private EventNotificationProperties eventNotificationProperties;

    @InjectMocks
    private EventUserNotificationFacade facade;

    // ─── helpers ──────────────────────────────────────────────────────────────────

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@mail.com");
        return user;
    }

    private Event event(String title, String rejectionReason) {
        Event event = new Event();
        event.setId(1L);
        event.setTitle(title);
        event.setRejectionReason(rejectionReason);
        return event;
    }

    // ─── notifyRejected ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("should create notification and send email when flag is ON")
    void notifyRejected_shouldCreateNotificationAndSendEmailWhenFlagEnabled() {
        User recipient = user(1L);
        Event event = event("Concierto Rock", "Incomplete information");

        when(eventNotificationProperties.getRejectedTitle()).thenReturn("Event rejected");

        facade.notifyRejected(recipient, event);

        verify(notificationService).create(recipient, "Event rejected", "Incomplete information");
        verify(emailService).sendEventRejectedEmail(recipient, event);
    }

    @Test
    @DisplayName("should create notification with null description when rejectionReason is null")
    void notifyRejected_shouldUseNullDescriptionWhenReasonIsNull() {
        User recipient = user(1L);
        Event event = event("Concierto", null);

        when(eventNotificationProperties.getRejectedTitle()).thenReturn("Event rejected");

        facade.notifyRejected(recipient, event);

        verify(notificationService).create(recipient, "Event rejected", null);
        verify(emailService).sendEventRejectedEmail(recipient, event);
    }

}
