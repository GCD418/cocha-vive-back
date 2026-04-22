package cocha.vive.backend.service;

import cocha.vive.backend.core.enums.AppFeature;
import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherRequestUserNotificationFacade {

    private static final String APPROVED_TITLE = "Solicitud aprobada";
    private static final String APPROVED_DESCRIPTION = "Now you're a Publisher";
    private static final String REJECTED_TITLE = "Solicitud rechazada";

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;

    public void notifyApproved(User recipientUser, PublisherRequest publisherRequest) {
        if (!isEnabled()) {
            log.debug("Skipping approved publisher request notification for user id {} because feature flag is OFF",
                recipientUser != null ? recipientUser.getId() : null);
            return;
        }

        notificationService.create(recipientUser, APPROVED_TITLE, APPROVED_DESCRIPTION);
        emailService.sendPublisherRequestApprovedEmail(recipientUser, publisherRequest);
    }

    public void notifyRejected(User recipientUser, PublisherRequest publisherRequest) {
        if (!isEnabled()) {
            log.debug("Skipping rejected publisher request notification for user id {} because feature flag is OFF",
                recipientUser != null ? recipientUser.getId() : null);
            return;
        }

        notificationService.create(recipientUser, REJECTED_TITLE, publisherRequest.getRejectionReason());
        emailService.sendPublisherRequestRejectedEmail(recipientUser, publisherRequest);
    }

    private boolean isEnabled() {
        return featureToggleService.isEnabled(AppFeature.NOTIFY_TO_USER_OF_PUBLISHER_REQUEST_CHANGES.getUnleashKey());
    }
}
