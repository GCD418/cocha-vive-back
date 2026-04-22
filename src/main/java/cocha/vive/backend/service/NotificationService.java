package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Notification;
import cocha.vive.backend.model.User;
import cocha.vive.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications() {
        Long actualUserId = auditService.getActualUserId();
        log.debug("Retrieving notifications for user id: {}", actualUserId);
        List<Notification> notifications = notificationRepository.findAllByUserIdOrderByCreatedAtAsc(actualUserId);
        log.debug("Retrieved {} notifications for user id: {}", notifications.size(), actualUserId);
        return notifications;
    }

    @Transactional
    public Notification create(User recipient, String title, String shortDescription) {
        Notification notification = Notification.builder()
            .notifiedUser(recipient)
            .title(title)
            .shortDescription(shortDescription)
            .build();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Long actualUserId = auditService.getActualUserId();
        log.info("Marking notification id: {} as read for user id: {}", notificationId, actualUserId);

        int updated = notificationRepository.markAsRead(notificationId, actualUserId);
        if (updated == 0) {
            log.warn("Notification id: {} not found for user id: {}", notificationId, actualUserId);
            throw new ResourceNotFoundException("Notification not found");
        }

        log.info("Notification id: {} marked as read for user id: {}", notificationId, actualUserId);
    }

    @Transactional
    public void markAllAsRead() {
        Long actualUserId = auditService.getActualUserId();
        log.info("Marking all unread notifications as read for user id: {}", actualUserId);

        int updated = notificationRepository.markAllAsReadByUserId(actualUserId);
        log.info("Marked {} notifications as read for user id: {}", updated, actualUserId);
    }

    @Transactional(readOnly = true)
    public long countMyUnreadNotifications() {
        Long actualUserId = auditService.getActualUserId();
        long unreadCount = notificationRepository.countUnreadByUserId(actualUserId);
        log.debug("Unread notifications for user id {}: {}", actualUserId, unreadCount);
        return unreadCount;
    }
}
