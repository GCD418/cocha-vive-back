package cocha.vive.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Payload for rejecting a publisher request")
public class PublisherRequestRejectDTO {

    @NotBlank
    @Size(min = 10, max = 200)
    @Schema(
        description = "Reason why the publisher request is being rejected",
        example = "The provided legal entity documentation is incomplete or invalid."
    )
    private String rejectionReason;
}
