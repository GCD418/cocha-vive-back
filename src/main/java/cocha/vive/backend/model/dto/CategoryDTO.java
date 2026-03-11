package cocha.vive.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String identifyingIcon;
}
