package cocha.vive.backend.service;

import cocha.vive.backend.config.EventNotificationProperties;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventUserNotificationFacade {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;
    private final EventNotificationProperties eventNotificationProperties;

    public void notifyRejected(User recipientUser, Event event) {
        notificationService.create(
            recipientUser,
            eventNotificationProperties.getRejectedTitle(),
            event.getRejectionReason()
        );
        emailService.sendEventRejectedEmail(recipientUser, event);
    }

}
