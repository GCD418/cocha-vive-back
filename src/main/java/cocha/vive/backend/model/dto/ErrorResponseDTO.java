package cocha.vive.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response payload")
public class ErrorResponseDTO {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Application error code", example = "RESOURCE_NOT_FOUND")
    private String code;

    @Schema(description = "Human-readable error message", example = "There is no category with name: Music")
    private String message;

    @Schema(description = "Timestamp when the error occurred", example = "2026-03-12T15:42:10")
    private LocalDateTime timestamp;
}