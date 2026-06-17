package cocha.vive.backend.auth;

import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.UserCreateDTO;
import cocha.vive.backend.service.EmailService;
import cocha.vive.backend.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private final UserService userService;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Operation(
        summary = "Login with Google",
        description = "Verifies a Google ID token and returns a signed JWT for the CochaVive API. " +
                      "A new user account is created automatically on first login."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful – JWT returned"),
        @ApiResponse(responseCode = "400", description = "Missing or blank token",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired Google ID token",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true)))
    })
    @SecurityRequirements   // no JWT required for the login endpoint
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody TokenDto tokenDto) {
        if (tokenDto == null || tokenDto.getToken() == null || tokenDto.getToken().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

            GoogleIdToken idToken = verifier.verify(tokenDto.getToken());

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();

                User user = userService.getByEmail(email).orElseGet(() -> {
                    String googleId = payload.getSubject();
                    String name = (String) payload.get("given_name");
                    String lastName = (String) payload.get("family_name");
                    String photoUrl = (String) payload.get("picture");
                    UserCreateDTO newUser = new UserCreateDTO();
                    newUser.setEmail(email);
                    newUser.setRole("ROLE_USER");
                    newUser.setGoogleProviderId(googleId);
                    newUser.setName(name);
                    newUser.setFirstLastName(lastName);
                    newUser.setPhotoUrl(photoUrl);
                    User createdUser = userService.create(newUser);
                    emailService.sendWelcomeEmail(createdUser);
                    return createdUser;
                });

                boolean requiresCI = (user.getDocumentNumber() == null || user.getDocumentNumber().trim().isEmpty());

                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("roles", user.getAuthorities());
                extraClaims.put("requiresOnboarding", requiresCI);

                String internalToken = jwtService.generateToken(extraClaims, user);
                return ResponseEntity.ok(new AuthResponse(internalToken, requiresCI));

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
