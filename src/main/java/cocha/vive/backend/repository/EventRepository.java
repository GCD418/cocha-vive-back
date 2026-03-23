package cocha.vive.backend.repository;

import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query(value = "SELECT * FROM events WHERE is_active = true AND date_start > NOW() AND event_status = 'APPROVED' " +
        "ORDER BY date_start ASC LIMIT 4", nativeQuery = true)
    List<Event> findActiveUpcoming();

    List<Event> findByIsActiveTrueAndIsFeaturedTrue();

    @Modifying
    @Query("UPDATE Event e SET e.eventStatus = :status, " +
        "e.updatedAt = CURRENT_TIMESTAMP, " +
        "e.modifiedByUserId = :userId " +
        "WHERE e.id = :id")
    int updateStatus(@Param("id") Long id,
                     @Param("status") EventStatus status,
                     @Param("userId") Long userId);

}
