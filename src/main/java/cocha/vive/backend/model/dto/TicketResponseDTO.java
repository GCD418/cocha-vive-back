package cocha.vive.backend.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponseDTO(
    UUID id,
    Integer quantity,
    Long unitPrice,
    Long totalPrice,
    Boolean expired,
    Boolean used,
    Long eventId,
    Long buyerUserId,
    LocalDateTime createdAt
) {
}
