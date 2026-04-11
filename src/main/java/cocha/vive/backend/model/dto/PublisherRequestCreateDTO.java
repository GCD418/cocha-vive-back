package cocha.vive.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Publisher request creation payload")
public class PublisherRequestCreateDTO {

    @NotBlank
    @Schema(description = "Reason for requesting publisher role", example = "We organize community cultural events weekly.")
    private String requestReason;

    @NotBlank
    @Schema(description = "Legal name of the person or organization", example = "Cocha Cultura SRL")
    private String legalEntityName;

    /*@NotEmpty
    @Schema(description = "Evidence image URLs supporting the request")
    private List<String> evidenceImages;*/
}
