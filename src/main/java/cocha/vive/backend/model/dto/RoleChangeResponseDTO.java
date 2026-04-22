package cocha.vive.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after a role change operation")
public record RoleChangeResponseDTO(
    @Schema(description = "User ID", example = "5")
    Long id,

    @Schema(description = "User email", example = "gabriel.perez@example.com")
    String email,

    @Schema(description = "Updated role", example = "ROLE_ADMIN")
    String role
) {}
