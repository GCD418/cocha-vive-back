package cocha.vive.backend.repository;

import cocha.vive.backend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query(value = "SELECT * FROM events WHERE is_active = true AND date_start > NOW() " +
        "ORDER BY date_start ASC LIMIT 4", nativeQuery = true)
    List<Event> findActiveUpcoming();

    List<Event> findByIsActiveTrueAndIsFeaturedTrue();

    List<Event> findByCategoryId(Long categoryId);
}
