package cocha.vive.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyTicketRequestDTO {
    @NotNull(message = "Event ID is required")
    @Schema(description = "ID of the event to purchase tickets for", example = "1")
    private Long eventId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity of tickets to purchase", example = "2")
    private Integer quantity;
}
