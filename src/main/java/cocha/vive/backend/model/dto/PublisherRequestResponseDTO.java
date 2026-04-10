package cocha.vive.backend.model.dto;

import cocha.vive.backend.model.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Publisher request response DTO")
public class PublisherRequestResponseDTO {

    private Long id;

    private String requestReason;

    private String legalEntityName;

    private List<String> evidenceImages;

    private UserMeDTO createdByUser;

    private RequestStatus requestStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isActive;

    private Long modifiedByUserId;
}
