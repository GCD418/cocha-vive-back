package cocha.vive.backend.model.dto;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.EventStatus;
import cocha.vive.backend.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Category request DTO")
public class EventCreateDTO {

    private String title;

    private String shortDescription;

    private String description;

    private Integer cost;

    private Category category;

    private User organizedByUser;

    private Double latitude;

    private Double longitude;

    private String shortPlaceDescription;

    @Column(name = "people_capacity", nullable = false)
    private Integer peopleCapacity;

    private LocalDateTime dateStart;

    private LocalDateTime dateEnd;

    private List<String> tags;

    private List<String> photoLinks;

    private EventStatus eventStatus = EventStatus.APPROVED;

    private User reviewedByAdminId;

}
