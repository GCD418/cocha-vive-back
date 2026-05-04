package cocha.vive.backend.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacebookAuthResponse {
    private String status;

    private String internalToken;

    private Boolean requiresOnboarding;

    private String registrationToken;

    private String facebookName;

    private String facebookPhotoUrl;
}
