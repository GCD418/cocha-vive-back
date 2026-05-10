package cocha.vive.backend.controller;

import cocha.vive.backend.auth.AuthResponse;
import cocha.vive.backend.auth.FacebookAuthResponse;
import cocha.vive.backend.auth.RegisterEmailRequest;
import cocha.vive.backend.auth.TokenDto;
import cocha.vive.backend.service.FacebookAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FacebookAuthController Tests")
public class FacebookAuthControllerTest {

    @Mock
    private FacebookAuthService facebookAuthService;

    @InjectMocks
    private FacebookAuthController facebookAuthController;

    private FacebookAuthResponse authResponse;
    private FacebookAuthResponse pendingResponse;
    private AuthResponse verifyResponse;

    @BeforeEach
    void setUp() {
        // Response para usuario existente
        authResponse = FacebookAuthResponse.builder()
            .status("AUTHENTICATED")
            .internalToken("jwt_token_123")
            .requiresOnboarding(false)
            .build();

        // Response para usuario nuevo
        pendingResponse = FacebookAuthResponse.builder()
            .status("PENDING_EMAIL_REGISTRATION")
            .registrationToken("temp_token_456")
            .facebookName("John Doe")
            .facebookPhotoUrl("https://example.com/photo.jpg")
            .build();

        // Response para verificación de email
        verifyResponse = new AuthResponse("final_jwt_789", true);
    }

    @Test
    @DisplayName("loginWithFacebook - Should authenticate existing user")
    void testLoginWithFacebookSuccess() {
        // Given
        TokenDto tokenDto = new TokenDto("valid_facebook_token");
        when(facebookAuthService.loginWithFacebook(tokenDto))
            .thenReturn(authResponse);

        // When
        ResponseEntity<FacebookAuthResponse> response = facebookAuthController
            .loginWithFacebook(tokenDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AUTHENTICATED", response.getBody().getStatus());
        assertEquals("jwt_token_123", response.getBody().getInternalToken());
        assertEquals(false, response.getBody().getRequiresOnboarding());
        assertNull(response.getBody().getRegistrationToken());

        verify(facebookAuthService).loginWithFacebook(tokenDto);
    }

    @Test
    @DisplayName("loginWithFacebook - Should return pending for new user")
    void testLoginWithFacebookNewUser() {
        // Given
        TokenDto tokenDto = new TokenDto("valid_facebook_token");
        when(facebookAuthService.loginWithFacebook(tokenDto))
            .thenReturn(pendingResponse);

        // When
        ResponseEntity<FacebookAuthResponse> response = facebookAuthController
            .loginWithFacebook(tokenDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PENDING_EMAIL_REGISTRATION", response.getBody().getStatus());
        assertEquals("temp_token_456", response.getBody().getRegistrationToken());
        assertEquals("John Doe", response.getBody().getFacebookName());
        assertEquals("https://example.com/photo.jpg", response.getBody().getFacebookPhotoUrl());

        verify(facebookAuthService).loginWithFacebook(tokenDto);
    }

    @Test
    @DisplayName("loginWithFacebook - Should return 401 for invalid token")
    void testLoginWithFacebookInvalidToken() {
        // Given
        TokenDto tokenDto = new TokenDto("invalid_token");
        when(facebookAuthService.loginWithFacebook(tokenDto))
            .thenThrow(new IllegalArgumentException("Invalid Facebook token"));

        // When
        ResponseEntity<FacebookAuthResponse> response = facebookAuthController
            .loginWithFacebook(tokenDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        verify(facebookAuthService).loginWithFacebook(tokenDto);
    }

    @Test
    @DisplayName("loginWithFacebook - Should return 400 for null token")
    void testLoginWithFacebookNullToken() {
        // Given
        TokenDto tokenDto = new TokenDto(null);

        // When
        ResponseEntity<FacebookAuthResponse> response = facebookAuthController
            .loginWithFacebook(tokenDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(facebookAuthService, never()).loginWithFacebook(any());
    }

    @Test
    @DisplayName("loginWithFacebook - Should return 400 for empty token")
    void testLoginWithFacebookEmptyToken() {
        // Given
        TokenDto tokenDto = new TokenDto("");

        // When
        ResponseEntity<FacebookAuthResponse> response = facebookAuthController
            .loginWithFacebook(tokenDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(facebookAuthService, never()).loginWithFacebook(any());
    }

    @Test
    @DisplayName("registerEmail - Should register email successfully")
    void testRegisterEmailSuccess() {
        // Given
        RegisterEmailRequest request = RegisterEmailRequest.builder()
            .registrationToken("temp_token_456")
            .email("john@example.com")
            .build();

        // When
        ResponseEntity<Void> response = facebookAuthController.registerEmail(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(facebookAuthService).registerEmail("temp_token_456", "john@example.com");
    }

    @Test
    @DisplayName("registerEmail - Should return 400 for invalid email")
    void testRegisterEmailInvalidFormat() {
        // Given
        RegisterEmailRequest request = RegisterEmailRequest.builder()
            .registrationToken("temp_token_456")
            .email("not_an_email")
            .build();

        doThrow(new IllegalArgumentException("Invalid email format"))
            .when(facebookAuthService).registerEmail("temp_token_456", "not_an_email");

        // When
        ResponseEntity<Void> response = facebookAuthController.registerEmail(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(facebookAuthService).registerEmail("temp_token_456", "not_an_email");
    }

    @Test
    @DisplayName("registerEmail - Should return 400 for duplicate email")
    void testRegisterEmailDuplicate() {
        // Given
        RegisterEmailRequest request = RegisterEmailRequest.builder()
            .registrationToken("temp_token_456")
            .email("existing@example.com")
            .build();

        doThrow(new IllegalArgumentException("Email already registered"))
            .when(facebookAuthService).registerEmail("temp_token_456", "existing@example.com");

        // When
        ResponseEntity<Void> response = facebookAuthController.registerEmail(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(facebookAuthService).registerEmail("temp_token_456", "existing@example.com");
    }

    @Test
    @DisplayName("registerEmail - Should return 400 for null request")
    void testRegisterEmailNullRequest() {
        // Given
        RegisterEmailRequest request = null;

        // When
        ResponseEntity<Void> response = facebookAuthController.registerEmail(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(facebookAuthService, never()).registerEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("registerEmail - Should return 400 for missing token")
    void testRegisterEmailMissingToken() {
        // Given
        RegisterEmailRequest request = RegisterEmailRequest.builder()
            .registrationToken(null)
            .email("john@example.com")
            .build();

        // When
        ResponseEntity<Void> response = facebookAuthController.registerEmail(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(facebookAuthService, never()).registerEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("verifyEmail - Should verify email successfully")
    void testVerifyEmailSuccess() {
        // Given
        String verificationToken = "valid_verification_token";
        when(facebookAuthService.verifyEmail(verificationToken))
            .thenReturn(verifyResponse);

        // When
        ResponseEntity<AuthResponse> response = facebookAuthController.verifyEmail(verificationToken);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("final_jwt_789", response.getBody().internalToken());
        assertEquals(true, response.getBody().requiresOnboarding());

        verify(facebookAuthService).verifyEmail(verificationToken);
    }

    @Test
    @DisplayName("verifyEmail - Should return 401 for invalid token")
    void testVerifyEmailInvalidToken() {
        // Given
        String invalidToken = "invalid_token";
        when(facebookAuthService.verifyEmail(invalidToken))
            .thenThrow(new IllegalArgumentException("Invalid verification token"));

        // When
        ResponseEntity<AuthResponse> response = facebookAuthController.verifyEmail(invalidToken);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        verify(facebookAuthService).verifyEmail(invalidToken);
    }

    @Test
    @DisplayName("verifyEmail - Should return 400 for null token")
    void testVerifyEmailNullToken() {
        // Given
        String token = null;

        // When
        ResponseEntity<AuthResponse> response = facebookAuthController.verifyEmail(token);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(facebookAuthService, never()).verifyEmail(anyString());
    }

    @Test
    @DisplayName("verifyEmail - Should return 400 for empty token")
    void testVerifyEmailEmptyToken() {
        // Given
        String token = "";

        // When
        ResponseEntity<AuthResponse> response = facebookAuthController.verifyEmail(token);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(facebookAuthService, never()).verifyEmail(anyString());
    }
}
