package cocha.vive.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Payload for rejecting an event")
public class EventRejectDTO {

    @Size(max = 200)
    @Schema(
        description = "Optional reason for event rejection",
        example = "Incomplete event information"
    )
    private String rejectionReason;
}
