package cocha.vive.backend.repository;

import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.QueryHints;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query(value = """
        SELECT * FROM events
        WHERE is_active = true
          AND event_status = 'APPROVED'
          AND date_end > NOW()
        ORDER BY date_start ASC
        """, nativeQuery = true)
    List<Event> findAllPublic();

    @Query(value = """
        SELECT e.id,
               e.title,
               e.short_description,
               e.description,
               e.cost,
               e.category_id,
               e.organized_by_user_id,
               e.latitude,
               e.longitude,
               e.short_place_description,
               e.people_capacity,
               e.date_start,
               e.date_end,
               e.tags,
               e.photo_links,
               e.is_featured,
               e.event_status,
               e.reviewed_by_admin_id,
               e.created_at,
               e.updated_at,
               e.is_active,
               e.modified_by_user_id
        FROM events e
        JOIN event_promotions p ON p.event_id = e.id
        WHERE e.is_active = true
          AND e.event_status = 'APPROVED'
          AND e.date_end > NOW()
          AND p.start_at <= NOW()
          AND p.end_at > NOW()
        ORDER BY p.end_at ASC, e.id ASC
        """, nativeQuery = true)
    List<Event> findActiveFeatured();

    @Query(value = """
        SELECT * FROM events
        WHERE is_active = true
          AND date_start > NOW()
          AND event_status = 'APPROVED'
        ORDER BY date_start ASC LIMIT 4
        """, nativeQuery = true)
    List<Event> findActiveUpcoming();

    @Query(value = """
        SELECT * FROM events
        WHERE is_active = true
          AND organized_by_user_id = :userId
        ORDER BY date_start DESC
        """, nativeQuery = true)
    List<Event> findAllByOrganizedByUserId(@Param("userId") Long userId);

    @Query(value = """
        SELECT * FROM events
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<Event> findAllForAdmin();

    @Modifying
    @Query("UPDATE Event e SET e.eventStatus = :status, " +
        "e.updatedAt = CURRENT_TIMESTAMP, " +
        "e.modifiedByUserId = :userId " +
        "WHERE e.id = :id")
    int updateStatus(@Param("id") Long id,
                     @Param("status") EventStatus status,
                     @Param("userId") Long userId);

    @QueryHints(@QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true"))
    List<Event> findByCategoryId(Long categoryId);
}
