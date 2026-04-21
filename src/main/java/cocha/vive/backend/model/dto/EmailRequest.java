package cocha.vive.backend.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailRequest (
    @NotBlank @Email
    String to,
    @NotBlank
    @Size(max = 200)
    String subject,
    @NotBlank
    String body
) {}
