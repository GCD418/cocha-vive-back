package cocha.vive.backend.model.dto;

import cocha.vive.backend.model.Category;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {

    private Long id;

    private String name;

    private String description;

    private String identifyingIcon;

    public static CategoryResponseDTO fromEntity(Category category){
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.id = category.getId();
        dto.name = category.getName();
        dto.description = category.getDescription();
        dto.identifyingIcon = category.getIdentifyingIcon();
        return dto;
    }
}
