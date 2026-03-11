package cocha.vive.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Category request DTO")
public class CategoryDTO {
    @NotBlank
    @Schema(description = "Category's name", example = "Music")
    private String name;

    @NotBlank
    @Schema(description = "Category's description", example = "Musical group playing songs")
    private String description;

    @NotBlank
    @Schema(description = "URL of category's identifying Icon", example = "https://cdn-icons-png.flaticon.com/512/2418/2418779.png")
    private String identifyingIcon;
}
