package cocha.vive.backend.service;

import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    @InjectMocks
    private PublisherRequestUserNotificationFacade facade;

    @Test
    void notifyApproved_shouldCreateNotificationAndSendEmailWhenFlagEnabled() {
        User recipient = user(1L);
        PublisherRequest request = request("Entidad", null);

        when(featureToggleService.isEnabled("notify-to-user-of-publisher-request-changes")).thenReturn(true);

        facade.notifyApproved(recipient, request);

        verify(notificationService).create(recipient, "Solicitud aprobada", "Now you're a Publisher");
        verify(emailService).sendPublisherRequestApprovedEmail(recipient, request);
    }

    @Test
    void notifyApproved_shouldDoNothingWhenFlagDisabled() {
        User recipient = user(1L);
        PublisherRequest request = request("Entidad", null);

        when(featureToggleService.isEnabled("notify-to-user-of-publisher-request-changes")).thenReturn(false);

        facade.notifyApproved(recipient, request);

        verify(notificationService, never()).create(any(), anyString(), anyString());
        verify(emailService, never()).sendPublisherRequestApprovedEmail(any(), any());
    }

    @Test
    void notifyRejected_shouldCreateNotificationAndSendEmailWhenFlagEnabled() {
        User recipient = user(1L);
        PublisherRequest request = request("Entidad", "Motivo de rechazo");

        when(featureToggleService.isEnabled("notify-to-user-of-publisher-request-changes")).thenReturn(true);

        facade.notifyRejected(recipient, request);

        verify(notificationService).create(recipient, "Solicitud rechazada", "Motivo de rechazo");
        verify(emailService).sendPublisherRequestRejectedEmail(recipient, request);
    }

    @Test
    void notifyRejected_shouldDoNothingWhenFlagDisabled() {
        User recipient = user(1L);
        PublisherRequest request = request("Entidad", "Motivo de rechazo");

        when(featureToggleService.isEnabled("notify-to-user-of-publisher-request-changes")).thenReturn(false);

        facade.notifyRejected(recipient, request);

        verify(notificationService, never()).create(any(), anyString(), anyString());
        verify(emailService, never()).sendPublisherRequestRejectedEmail(any(), any());
    }

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
