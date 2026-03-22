package cocha.vive.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "User creation request DTO")
public class UserCreateDTO {

    @Schema(description = "User first names", example = "Gabriel")
    private String name;

    @Schema(description = "User first last name", example = "Perez")
    private String firstLastName;

    @Schema(description = "User email", example = "gabriel.perez@example.com")
    private String email;

    @Schema(description = "Google provider unique identifier", example = "110012341234123412341")
    private String googleProviderId;

    @Schema(description = "User document number", example = "8349271")
    private String documentNumber;

    @Schema(description = "Public photo URL", example = "https://example.com/photos/user-1.jpg")
    private String photoUrl;

    @Schema(description = "Assigned application role", example = "ROLE_USER")
    private String role;
}
