package cocha.vive.backend.controller;

import cocha.vive.backend.model.Notification;
import cocha.vive.backend.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController tests")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Nested
    @DisplayName("GET /api/notifications")
    class GetMyNotifications {

        @Test
        void shouldReturnNotifications() {
            Notification n1 = Notification.builder().id(1L).title("Primera").build();
            Notification n2 = Notification.builder().id(2L).title("Segunda").build();
            when(notificationService.getMyNotifications()).thenReturn(List.of(n1, n2));

            ResponseEntity<List<Notification>> response = notificationController.getMyNotifications();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
            verify(notificationService).getMyNotifications();
        }
    }

    @Nested
    @DisplayName("PATCH /api/notifications/{id}/read")
    class MarkAsRead {

        @Test
        void shouldReturnNoContent() {
            doNothing().when(notificationService).markAsRead(7L);

            ResponseEntity<Void> response = notificationController.markAsRead(7L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(notificationService).markAsRead(7L);
        }
    }
}
