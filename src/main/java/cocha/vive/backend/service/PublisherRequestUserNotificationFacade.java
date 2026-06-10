package cocha.vive.backend.service;

import cocha.vive.backend.config.PublisherRequestNotificationProperties;
import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherRequestUserNotificationFacade {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;
    private final PublisherRequestNotificationProperties publisherRequestNotificationProperties;
    private static final String DEMOTED_FROM_PUBLISHER_TITLE = "Your publisher access has been revoked";

    public void notifyApproved(User recipientUser, PublisherRequest publisherRequest) {
        notificationService.create(
            recipientUser,
            publisherRequestNotificationProperties.getApprovedTitle(),
            publisherRequestNotificationProperties.getApprovedDescription()
        );
        emailService.sendPublisherRequestApprovedEmail(recipientUser, publisherRequest);
    }

    public void notifyRejected(User recipientUser, PublisherRequest publisherRequest) {
        notificationService.create(
            recipientUser,
            publisherRequestNotificationProperties.getRejectedTitle(),
            publisherRequest.getRejectionReason()
        );
        emailService.sendPublisherRequestRejectedEmail(recipientUser, publisherRequest);
    }

    public void notifyDemotedFromPublisher(User recipient, String demotionReason) {
        notificationService.create(
            recipient,
            DEMOTED_FROM_PUBLISHER_TITLE,
            demotionReason
        );
        emailService.sendPublisherDemotionEmail(recipient, demotionReason);
    }
}
