package cocha.vive.backend.model.dto;

import cocha.vive.backend.model.Currency;
import cocha.vive.backend.model.PromotionPlan;

import java.time.LocalDateTime;
import java.util.UUID;

public record PromotionResponseDTO(
    UUID id,
    Long eventId,
    String eventTitle,
    PromotionPlan plan,
    Long amount,
    Currency currency,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime createdAt
) {
}
