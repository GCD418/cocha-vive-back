package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Notification;
import cocha.vive.backend.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void getMyNotifications_shouldReturnNotificationsOrderedByRepositoryQuery() {
        Long userId = 20L;
        Notification first = Notification.builder().id(1L).title("A").build();
        Notification second = Notification.builder().id(2L).title("B").build();

        when(auditService.getActualUserId()).thenReturn(userId);
        when(notificationRepository.findAllByUserIdOrderByCreatedAtAsc(userId)).thenReturn(List.of(first, second));

        List<Notification> result = notificationService.getMyNotifications();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(notificationRepository).findAllByUserIdOrderByCreatedAtAsc(userId);
    }

    @Test
    void markAsRead_shouldUpdateNotificationForCurrentUser() {
        Long userId = 20L;
        Long notificationId = 99L;

        when(auditService.getActualUserId()).thenReturn(userId);
        when(notificationRepository.markAsRead(notificationId, userId)).thenReturn(1);

        notificationService.markAsRead(notificationId);

        verify(notificationRepository).markAsRead(notificationId, userId);
    }

    @Test
    void markAsRead_shouldThrowWhenNotificationNotFoundForUser() {
        Long userId = 20L;
        Long notificationId = 404L;

        when(auditService.getActualUserId()).thenReturn(userId);
        when(notificationRepository.markAsRead(notificationId, userId)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class,
            () -> notificationService.markAsRead(notificationId));
    }
}
