package cocha.vive.backend.service;

import cocha.vive.backend.auth.*;
import cocha.vive.backend.model.User;
import cocha.vive.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final FacebookTokenVerifier facebookTokenVerifier;

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public FacebookAuthResponse loginWithFacebook(TokenDto tokenDto) {
        log.info("Facebook login attempt");

        FacebookPayload payload = facebookTokenVerifier.verify(tokenDto.getToken());

        boolean isPerson = payload.isPerson();
        log.info("Facebook login type detected: {}", isPerson ? "PERSON" : "PAGE");

        Optional<User> existing;

        if (isPerson) {
            existing = userRepository.findByFacebookProviderId(payload.getId());
        } else {
            existing = userRepository.findByFacebookPageId(payload.getId());
        }

        if (existing.isPresent()) {
            log.info("Existing Facebook user found");
            User user = existing.get();

            Map<String, Object> extraClaims = Map.of(
                "roles", user.getAuthorities()
            );

            String internalToken = jwtService.generateToken(extraClaims, user);

            return FacebookAuthResponse.builder()
            .status("AUTHENTICATED")
            .internalToken(internalToken)
            .requiresOnboarding(false)
            .build();
        }

        log.info("New Facebook user/page - generating temporary registration token");

        Map<String, Object> claims = Map.of(
            "facebookId", payload.getId(),
            "name", payload.getName(),
            "email", payload.getEmail() != null ? payload.getEmail() : "",
            "firstName", payload.getFirstName() != null ? payload.getFirstName() : "",
            "lastName", payload.getLastName() != null ? payload.getLastName() : "",
            "photoUrl", payload.getPictureUrl() != null ? payload.getPictureUrl() : "",
            "isPerson", isPerson,  // ← Guarda si es persona o página
            "purpose", "EMAIL_REGISTRATION"
        );

        String registrationToken = jwtService
            .generateTokenWithExpiration(claims, null, 86400);  // 24 horas

        return FacebookAuthResponse.builder()
        .status("PENDING_EMAIL_REGISTRATION")
        .registrationToken(registrationToken)
        .facebookName(payload.getName())
        .facebookPhotoUrl(payload.getPictureUrl())
        .build();
    }

    @Transactional
    public void registerEmail(String registrationToken, String email) {
        log.info("Registering email for Facebook user");

        Map<String, Object> claims = jwtService
            .extractAllClaimsAsMap(registrationToken);

        if (!"EMAIL_REGISTRATION".equals(claims.get("purpose"))) {
            log.warn("Invalid token purpose for email registration");
            throw new IllegalArgumentException("Invalid registration token");
        }

        String facebookId = (String) claims.get("facebookId");
        boolean isPerson = (boolean) claims.get("isPerson");

        validateEmail(email);

        log.info("Email validated: {}", email);

        Map<String, Object> verificationClaims = Map.of(
            "facebookId", facebookId,
            "email", email,
            "name", claims.get("name"),
            "firstName", claims.get("firstName"),
            "lastName", claims.get("lastName"),
            "photoUrl", claims.get("photoUrl"),
            "isPerson", isPerson,
            "purpose", "VERIFY_EMAIL"
        );

        String verificationToken = jwtService
            .generateTokenWithExpiration(verificationClaims, null, 172800);  // 48 horas

        emailService.sendEmailVerificationEmail(email, verificationToken);

        log.info("Verification email sent to: {}", email);
    }

    @Transactional
    public AuthResponse verifyEmail(String verificationToken) {
        log.info("Verifying email for Facebook user");

        Map<String, Object> claims = jwtService
            .extractAllClaimsAsMap(verificationToken);

        if (!"VERIFY_EMAIL".equals(claims.get("purpose"))) {
            log.warn("Invalid token purpose for email verification");
            throw new IllegalArgumentException("Invalid verification token");
        }

        String facebookId = (String) claims.get("facebookId");
        String email = (String) claims.get("email");
        String name = (String) claims.get("name");
        String firstName = (String) claims.get("firstName");
        String lastName = (String) claims.get("lastName");
        String photoUrl = (String) claims.get("photoUrl");
        boolean isPerson = (boolean) claims.get("isPerson");

        log.info("Creating new Facebook user/page: email={}, isPerson={}", email, isPerson);

        User user = User.builder()
            .email(email)
            .names(name)
            .photoUrl(!photoUrl.isEmpty() ? photoUrl : null)
            .role("ROLE_PUBLISHER")
            .isActive(true)
            .build();

        if (isPerson) {
            user.setFacebookProviderId(facebookId);
            user.setFirstLastName(lastName);
        } else {
            user.setFacebookPageId(facebookId);
        }

        userRepository.save(user);

        log.info("Facebook user/page created: id={}, isPerson={}", user.getId(), isPerson);

        Map<String, Object> extraClaims = Map.of(
            "roles", user.getAuthorities()
        );

        String internalToken = jwtService.generateToken(extraClaims, user);

        return new AuthResponse(internalToken, true);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Email already registered: {}", email);
            throw new IllegalArgumentException("Email already registered");
        }

        log.debug("Email validation passed: {}", email);
    }
}
