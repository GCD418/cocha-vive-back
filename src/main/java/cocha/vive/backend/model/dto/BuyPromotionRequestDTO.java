package cocha.vive.backend.model.dto;

import cocha.vive.backend.model.PromotionPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyPromotionRequestDTO {

    @NotNull(message = "Event ID is required")
    @Schema(description = "ID of the event to promote", example = "1")
    private Long eventId;

    @NotNull(message = "Promotion plan is required")
    @Schema(description = "Promotion package to purchase",
        example = "ONE_WEEK",
        allowableValues = {"ONE_DAY", "THREE_DAYS", "ONE_WEEK"})
    private PromotionPlan plan;
}
