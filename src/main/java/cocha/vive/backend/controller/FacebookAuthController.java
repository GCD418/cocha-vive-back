package cocha.vive.backend.controller;

import cocha.vive.backend.auth.TokenDto;
import cocha.vive.backend.service.FacebookAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/api/auth/facebook")
@RequiredArgsConstructor
public class FacebookAuthController {

    private final FacebookAuthService facebookAuthService;

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
    }

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
