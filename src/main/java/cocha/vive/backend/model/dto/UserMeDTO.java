package cocha.vive.backend.model.dto;

import cocha.vive.backend.model.User;

public record UserMeDTO(
    Long id,
    String names,
    String firstLastName,
    String secondLastName,
    String email,
    String photoUrl
) {
    public UserMeDTO(User user) {
        this(
            user.getId(),
            user.getNames(),
            user.getFirstLastName(),
            user.getSecondLastName(),
            user.getEmail(),
            user.getPhotoUrl()
        );
    }
}
