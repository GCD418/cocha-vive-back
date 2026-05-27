package cocha.vive.backend.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponseDTO(
    Long id,
    String title,
    String shortDescription,
    String description,
    Integer cost,
    Long categoryId,
    String categoryName,
    Long organizedByUserId,
    String organizedByUserName,
    Double latitude,
    Double longitude,
    String shortPlaceDescription,
    Integer peopleCapacity,
    LocalDateTime dateStart,
    LocalDateTime dateEnd,
    List<String> tags,
    List<String> photoLinks,
    String eventStatus,
    Boolean isActive,
    LocalDateTime createdAt,

    Boolean isFeatured,
    String promotionType,
    String promotionSlot,
    LocalDateTime expiresAt
) {
}
