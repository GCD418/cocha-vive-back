package cocha.vive.backend.controller;

import cocha.vive.backend.auth.AuthResponse;
import cocha.vive.backend.auth.FacebookAuthResponse;
import cocha.vive.backend.auth.TokenDto;
import cocha.vive.backend.auth.RegisterEmailRequest;
import cocha.vive.backend.core.annotations.FeatureFlag;
import cocha.vive.backend.core.enums.AppFeature;
import cocha.vive.backend.service.FacebookAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Facebook Authentication", description = "Facebook OAuth2 login and email verification endpoints")
@Log4j2
@FeatureFlag(AppFeature.FACEBOOK_LOGIN)
@RestController
@RequestMapping("/api/auth/facebook")
@RequiredArgsConstructor
public class FacebookAuthController {

    private final FacebookAuthService facebookAuthService;

    @Operation(summary = "Authenticate with Facebook", description = "Verifies Facebook OAuth token and returns JWT for authenticated users or temporary token for new users requiring email registration")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content = @Content(schema = @Schema(implementation = FacebookAuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or empty token provided"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid Facebook token or token verification failed"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during token verification"
        )
    })
    @PostMapping
    public ResponseEntity<FacebookAuthResponse> loginWithFacebook(
            @RequestBody TokenDto tokenDto) {

        if (tokenDto == null || tokenDto.getToken() == null ||
            tokenDto.getToken().isBlank()) {
            log.warn("Empty token provided for Facebook login");
            return ResponseEntity.badRequest().build();
        }

        try {
            FacebookAuthResponse response = facebookAuthService
                .loginWithFacebook(tokenDto);

            log.info("Facebook login successful: status={}", response.getStatus());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid Facebook token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {
            log.error("Facebook authentication error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @Operation(summary = "Register email for new Facebook user", description = "Validates and registers email address for new users. Sends verification email with confirmation link.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email registered successfully, verification email sent"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid email format, duplicate email, or missing required fields"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during email registration"
        )
    })
    @PostMapping("/register-email")
    public ResponseEntity<Void> registerEmail(
            @RequestBody RegisterEmailRequest request) {

        if (request == null || request.getRegistrationToken() == null ||
            request.getEmail() == null) {
            log.warn("Missing required fields for email registration");
            return ResponseEntity.badRequest().build();
        }

        try {
            facebookAuthService.registerEmail(
                request.getRegistrationToken(),
                request.getEmail()
            );

            log.info("Email registered successfully");
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.warn("Email registration validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Email registration error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Verify email and create user account", description = "Verifies email confirmation token and creates user account in the system. Returns JWT token for immediate login.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email verified successfully, user created",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Missing or empty verification token"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired verification token"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during email verification"
        )
    })
    @GetMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(
            @RequestParam String token) {

        if (token == null || token.isBlank()) {
            log.warn("Empty verification token provided");
            return ResponseEntity.badRequest().build();
        }

        try {
            AuthResponse response = facebookAuthService.verifyEmail(token);

            log.info("Email verified successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid verification token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {
            log.error("Email verification error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
