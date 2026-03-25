package cocha.vive.backend.model.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventRequest {
    private String title;
    private String shortDescription;
    private String description;
    private Integer cost;
    private Long categoryId;
    private Double latitude;
    private Double longitude;
    private String shortPlaceDescription;
    private Integer peopleCapacity;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private List<String> tags;
    private List<String> photoLinks;
}
