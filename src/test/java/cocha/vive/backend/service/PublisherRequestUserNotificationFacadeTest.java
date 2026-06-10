package cocha.vive.backend.service;

import cocha.vive.backend.config.PublisherRequestNotificationProperties;
import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublisherRequestUserNotificationFacade tests")
class PublisherRequestUserNotificationFacadeTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private PublisherRequestNotificationProperties publisherRequestNotificationProperties;

    @InjectMocks
    private PublisherRequestUserNotificationFacade facade;

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user@mail.com");
        return user;
    }

    private PublisherRequest request(String legalEntityName, String rejectionReason) {
        PublisherRequest request = new PublisherRequest();
        request.setLegalEntityName(legalEntityName);
        request.setRejectionReason(rejectionReason);
        return request;
    }
}
