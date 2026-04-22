package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Notification;
import cocha.vive.backend.model.User;
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

    @Test
    void create_shouldPersistNotification() {
        User recipient = new User();
        recipient.setId(20L);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification result = notificationService.create(recipient, "Solicitud aprobada", "Now you're a Publisher");

        assertThat(result.getNotifiedUser()).isEqualTo(recipient);
        assertThat(result.getTitle()).isEqualTo("Solicitud aprobada");
        assertThat(result.getShortDescription()).isEqualTo("Now you're a Publisher");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void markAllAsRead_shouldUpdateAllUnreadNotificationsForCurrentUser() {
        Long userId = 20L;

        when(auditService.getActualUserId()).thenReturn(userId);
        when(notificationRepository.markAllAsReadByUserId(userId)).thenReturn(3);

        notificationService.markAllAsRead();

        verify(notificationRepository).markAllAsReadByUserId(userId);
    }

    @Test
    void countMyUnreadNotifications_shouldReturnUnreadCountForCurrentUser() {
        Long userId = 20L;

        when(auditService.getActualUserId()).thenReturn(userId);
        when(notificationRepository.countUnreadByUserId(userId)).thenReturn(5L);

        long result = notificationService.countMyUnreadNotifications();

        assertThat(result).isEqualTo(5L);
        verify(notificationRepository).countUnreadByUserId(userId);
    }
}
