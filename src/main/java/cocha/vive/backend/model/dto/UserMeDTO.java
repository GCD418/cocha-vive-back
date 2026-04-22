package cocha.vive.backend.model.dto;

import cocha.vive.backend.model.User;
import java.time.LocalDateTime;

public record UserMeDTO(
    Long id,
    String names,
    String firstLastName,
    String secondLastName,
    String email,
    String photoUrl,
    String role,
    LocalDateTime createdAt
) {
    public UserMeDTO(User user) {
        this(
            user.getId(),
            user.getNames(),
            user.getFirstLastName(),
            user.getSecondLastName(),
            user.getEmail(),
            user.getPhotoUrl(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
}
