package cocha.vive.backend.repository;

import cocha.vive.backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = """
        SELECT *
        FROM notifications
        WHERE notified_user = :userId AND unread = true
        ORDER BY created_at ASC
        """, nativeQuery = true)
    List<Notification> findAllByUserIdOrderByCreatedAtAsc(@Param("userId") Long userId);

    @Modifying
    @Query(value = """
        UPDATE notifications
        SET unread = false
        WHERE id = :notificationId
          AND notified_user = :userId
        """, nativeQuery = true)
    int markAsRead(@Param("notificationId") Long notificationId,
                   @Param("userId") Long userId);
}
