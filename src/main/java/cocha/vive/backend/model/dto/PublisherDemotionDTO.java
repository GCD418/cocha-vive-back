package cocha.vive.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for demoting a publisher back to ROLE_USER")
public record PublisherDemotionDTO (
    @NotBlank(message = "Demotion reason must not be blank")
    @Size(min = 10, max = 500, message = "Demotion reason must be between 10 and 500 characters")
    @Schema(
        description = "Reason for demoting the publisher",
        example = "Publisher repeatedly violated content guidelines."
    )
    String demotionReason
)  {}
