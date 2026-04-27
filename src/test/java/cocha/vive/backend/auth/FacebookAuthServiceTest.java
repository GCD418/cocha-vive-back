package cocha.vive.backend.auth;

import cocha.vive.backend.model.User;
import cocha.vive.backend.repository.UserRepository;
import cocha.vive.backend.service.FacebookAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.doNothing;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FacebookAuthService Tests")
public class FacebookAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private cocha.vive.backend.service.EmailService emailService;

    @Mock
    private FacebookTokenVerifier facebookTokenVerifier;

    @InjectMocks
    private FacebookAuthService facebookAuthService;

    private FacebookPayload facebookPayloadPerson;
    private FacebookPayload facebookPayloadPage;
    private User existingUser;

    @BeforeEach
    void setUp() {
        // Crear payload de persona

        FacebookPayload.Picture picture = new FacebookPayload.Picture();
        picture.setData(new FacebookPayload.Picture.PictureData());
        picture.getData().setUrl("https://example.com/photo.jpg");

        facebookPayloadPerson = FacebookPayload.builder()
            .id("123456789")
            .name("John Doe")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .picture(picture)
            .build();

        FacebookPayload.Picture picturePage = new FacebookPayload.Picture();
        picturePage.setData(new FacebookPayload.Picture.PictureData());
        picturePage.getData().setUrl("https://example.com/logo.jpg");

        // Crear payload de página
        facebookPayloadPage = FacebookPayload.builder()
            .id("987654321")
            .name("Test Organization")
            .firstName(null)
            .lastName(null)
            .email(null)
            .picture(picturePage)
            .build();

        // Crear usuario existente
        existingUser = User.builder()
            .id(1L)
            .email("john.doe@example.com")
            .names("John Doe")
            .facebookProviderId("123456789")
            .role("ROLE_PUBLISHER")
            .build();
            }

    @Test
    @DisplayName("Should authenticate existing person Facebook user")
    void testLoginWithExistingPerson() {
        // Given
        TokenDto tokenDto = new TokenDto("valid_token");
        when(facebookTokenVerifier.verify(anyString()))
            .thenReturn(facebookPayloadPerson);
        when(userRepository.findByFacebookProviderId("123456789"))
            .thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(any(), any(User.class)))
            .thenReturn("jwt_token");

        // When
        FacebookAuthResponse response = facebookAuthService.loginWithFacebook(tokenDto);

        // Then
        assertNotNull(response);
        assertEquals("AUTHENTICATED", response.getStatus());
        assertEquals("jwt_token", response.getInternalToken());
        assertEquals(false, response.getRequiresOnboarding());
        assertNull(response.getRegistrationToken());

        verify(userRepository).findByFacebookProviderId("123456789");
        verify(jwtService).generateToken(any(), eq(existingUser));
    }

    @Test
    @DisplayName("Should detect new person and return pending email registration")
    void testLoginWithNewPerson() {
        // Given
        TokenDto tokenDto = new TokenDto("valid_token");
        when(facebookTokenVerifier.verify(anyString()))
            .thenReturn(facebookPayloadPerson);
        when(userRepository.findByFacebookProviderId("123456789"))
            .thenReturn(Optional.empty());
        when(jwtService.generateTokenWithExpiration(any(), any(), anyLong()))
            .thenReturn("temporary_token");

        // When
        FacebookAuthResponse response = facebookAuthService.loginWithFacebook(tokenDto);

        // Then
        assertNotNull(response);
        assertEquals("PENDING_EMAIL_REGISTRATION", response.getStatus());
        assertEquals("temporary_token", response.getRegistrationToken());
        assertEquals("John Doe", response.getFacebookName());
        assertEquals("https://example.com/photo.jpg", response.getFacebookPhotoUrl());

        verify(userRepository).findByFacebookProviderId("123456789");
        verify(jwtService).generateTokenWithExpiration(any(), any(), eq(86400L));
    }

    @Test
    @DisplayName("Should detect new page and return pending email registration")
    void testLoginWithNewPage() {
        // Given
        TokenDto tokenDto = new TokenDto("valid_token");
        when(facebookTokenVerifier.verify(anyString()))
            .thenReturn(facebookPayloadPage);
        when(userRepository.findByFacebookPageId("987654321"))
            .thenReturn(Optional.empty());
        when(jwtService.generateTokenWithExpiration(any(), any(), anyLong()))
            .thenReturn("temporary_token");

        // When
        FacebookAuthResponse response = facebookAuthService.loginWithFacebook(tokenDto);

        // Then
        assertNotNull(response);
        assertEquals("PENDING_EMAIL_REGISTRATION", response.getStatus());
        assertEquals("temporary_token", response.getRegistrationToken());
        assertEquals("Test Organization", response.getFacebookName());

        verify(userRepository).findByFacebookPageId("987654321");
    }

    @Test
    @DisplayName("Should reject invalid email format")
    void testRegisterEmailWithInvalidFormat() {
        // Given
        String registrationToken = "valid_token";
        String invalidEmail = "not_an_email";

        when(jwtService.extractAllClaimsAsMap(registrationToken))
            .thenReturn(java.util.Map.of(
                "facebookId", "123456789",
                "isPerson", true,
                "purpose", "EMAIL_REGISTRATION"
            ));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            facebookAuthService.registerEmail(registrationToken, invalidEmail);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject duplicate email")
    void testRegisterEmailWithDuplicateEmail() {
        // Given
        String registrationToken = "valid_token";
        String email = "existing@example.com";

        when(jwtService.extractAllClaimsAsMap(registrationToken))
            .thenReturn(java.util.Map.of(
                "facebookId", "123456",
                "isPerson", true,
                "purpose", "EMAIL_REGISTRATION"
            ));
        when(userRepository.findByEmail(email))
            .thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            facebookAuthService.registerEmail(registrationToken, email);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully validate and register valid email")
    void testRegisterEmailWithValidEmail() {
        // Given
        String registrationToken = "valid_token";
        String validEmail = "newuser@example.com";

        when(jwtService.extractAllClaimsAsMap(registrationToken))
            .thenReturn(java.util.Map.of(
                "facebookId", "123456789",
                "isPerson", true,
                "purpose", "EMAIL_REGISTRATION"
            ));
        when(userRepository.findByEmail(validEmail))
            .thenReturn(Optional.empty());
        when(jwtService.generateTokenWithExpiration(any(), any(), eq(172800L)))
            .thenReturn("verification_token");

        // Mock el envío de email
        doNothing().when(emailService).sendEmailVerificationEmail(validEmail, "verification_token");

        // When
        facebookAuthService.registerEmail(registrationToken, validEmail);

        // Then
        verify(userRepository).findByEmail(validEmail);
        verify(emailService).sendEmailVerificationEmail(validEmail, "verification_token");
    }

    @Test
    @DisplayName("Should create person user after email verification")
    void testVerifyEmailAndCreatePerson() {
        // Given
        String verificationToken = "valid_token";

        when(jwtService.extractAllClaimsAsMap(verificationToken))
            .thenReturn(java.util.Map.of(
                "facebookId", "123456789",
                "email", "john.doe@example.com",
                "name", "John Doe",
                "firstName", "John",
                "lastName", "Doe",
                "photoUrl", "https://example.com/photo.jpg",
                "isPerson", true,
                "purpose", "VERIFY_EMAIL"
            ));
        when(userRepository.save(any(User.class)))
            .thenReturn(existingUser);
        when(jwtService.generateToken(any(), any(User.class)))
            .thenReturn("final_jwt");

        // When
        AuthResponse response = facebookAuthService.verifyEmail(verificationToken);

        // Then
        assertNotNull(response);
        assertEquals("final_jwt", response.internalToken());
        assertEquals(true, response.requiresOnboarding());

        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(), any(User.class));
    }

    @Test
    @DisplayName("Should create page user after email verification")
    void testVerifyEmailAndCreatePage() {
        // Given
        String verificationToken = "valid_token";
        User pageUser = User.builder()
            .id(2L)
            .email("jefe.cultura@alcaldia.gob.bo")
            .names("Alcaldía de La Paz")
            .facebookPageId("987654321")
            .role("ROLE_PUBLISHER")
            .build();

        when(jwtService.extractAllClaimsAsMap(verificationToken))
            .thenReturn(java.util.Map.of(
                "facebookId", "987654321",
                "email", "jefe.cultura@alcaldia.gob.bo",
                "name", "Alcaldía de La Paz",
                "firstName", "",
                "lastName", "",
                "photoUrl", "https://example.com/logo.jpg",
                "isPerson", false,
                "purpose", "VERIFY_EMAIL"
            ));
        when(userRepository.save(any(User.class)))
            .thenReturn(pageUser);
        when(jwtService.generateToken(any(), any(User.class)))
            .thenReturn("final_jwt");

        // When
        AuthResponse response = facebookAuthService.verifyEmail(verificationToken);

        // Then
        assertNotNull(response);
        assertEquals("final_jwt", response.internalToken());
        assertEquals(true, response.requiresOnboarding());

        verify(userRepository).save(any(User.class));
    }
}
