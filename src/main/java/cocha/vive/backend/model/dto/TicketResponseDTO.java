package cocha.vive.backend.model.dto;

import java.time.LocalDateTime;

public record TicketResponseDTO(
    Long id,
    Integer quantity,
    Long unitPrice,
    Long totalPrice,
    Boolean used,
    Long eventId,
    Long buyerUserId,
    LocalDateTime createdAt
) {
}
