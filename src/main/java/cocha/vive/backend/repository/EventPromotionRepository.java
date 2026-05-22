package cocha.vive.backend.repository;

import cocha.vive.backend.model.EventPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventPromotionRepository extends JpaRepository<EventPromotion, UUID> {

    Optional<EventPromotion> findTopByEventIdOrderByEndAtDesc(Long eventId);

    @Query("""
        SELECT p FROM EventPromotion p
        WHERE p.event.id = :eventId
          AND p.startAt <= :now
          AND p.endAt > :now
        ORDER BY p.endAt DESC
        """)
    Optional<EventPromotion> findActivePromotion(@Param("eventId") Long eventId,
                                                 @Param("now") LocalDateTime now);

}
