package cocha.vive.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to complete a user's profile")
public record CompleteProfileDto(
	@NotBlank
	@Size(max = 30)
	@Schema(description = "Document number of the user", example = "8349271")
	String documentNumber,

	@NotBlank
	@Pattern(regexp = "^[0-9A-Z]{2}$")
	@Schema(description = "Two-letter document extension in some duplicated document numbers", example = "1H")
	String extension
) {
}
