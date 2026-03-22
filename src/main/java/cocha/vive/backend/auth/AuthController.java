package cocha.vive.backend.auth;

import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.UserCreateDTO;
import cocha.vive.backend.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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
public class AuthController {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private final UserService userService;
    private final JwtService jwtService;

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
                String googleId = payload.getSubject();
                String name = (String) payload.get("given_name");
                String lastName = (String) payload.get("family_name");
                String photoUrl = (String) payload.get("picture");

                User user = userService.getByEmail(email).orElseGet(() -> {
                    UserCreateDTO newUser = new UserCreateDTO();
                    newUser.setEmail(email);
                    newUser.setRole("ROLE_USER");
                    newUser.setGoogleProviderId(googleId);
                    newUser.setName(name);
                    newUser.setFirstLastName(lastName);
                    newUser.setDocumentNumber(photoUrl);
                    return userService.create(newUser);
                });

                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("roles", user.getAuthorities());

                String internalToken = jwtService.generateToken(extraClaims, user);
                return ResponseEntity.ok(new AuthResponse(internalToken));

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
