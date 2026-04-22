package cocha.vive.backend.controller;

import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.CompleteProfileDto;
import cocha.vive.backend.model.dto.UserMeDTO;
import cocha.vive.backend.model.mapper.UserMapper;
import cocha.vive.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("gabriel.perez@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    private User buildUser() {
        return User.builder()
            .id(1L)
            .names("Gabriel")
            .firstLastName("Perez")
            .secondLastName("Quispe")
            .email("gabriel.perez@example.com")
            .photoUrl("https://example.com/photos/user-1.jpg")
            .role("ROLE_USER")
            .isActive(true)
            .build();
    }

    // ─── PUT /api/users/complete-profile ────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/users/complete-profile")
    class CompleteProfile {

        @Test
        @DisplayName("200 – actualiza perfil correctamente")
        void shouldReturn200WhenProfileUpdatedSuccessfully() {
            CompleteProfileDto dto = new CompleteProfileDto("8349271", "1H");
            doNothing().when(userService)
                .updateDocumentNumber("gabriel.perez@example.com", "8349271", "1H");

            ResponseEntity<Void> response = userController.completeProfile(dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService, times(1))
                .updateDocumentNumber("gabriel.perez@example.com", "8349271", "1H");
        }

        @Test
        @DisplayName("usa el email del SecurityContext, no uno arbitrario")
        void shouldUseEmailFromSecurityContext() {
            CompleteProfileDto dto = new CompleteProfileDto("8349271", "1H");
            doNothing().when(userService)
                .updateDocumentNumber(anyString(), anyString(), anyString());

            userController.completeProfile(dto);

            verify(userService).updateDocumentNumber(
                eq("gabriel.perez@example.com"), anyString(), anyString()
            );
        }

        @Test
        @DisplayName("llama al servicio con los datos correctos del DTO")
        void shouldPassDtoDataToService() {
            CompleteProfileDto dto = new CompleteProfileDto("9999999", "2A");
            doNothing().when(userService)
                .updateDocumentNumber(anyString(), anyString(), anyString());

            userController.completeProfile(dto);

            verify(userService).updateDocumentNumber(
                "gabriel.perez@example.com", "9999999", "2A"
            );
        }
    }

    // ─── GET /api/users/me ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/users/me")
    class GetCurrentUser {

        @Test
        @DisplayName("200 – retorna UserMeDTO del usuario autenticado")
        void shouldReturn200WithUserMeDTO() {
            User user = buildUser();
            UserMeDTO dto = new UserMeDTO(
                1L,
                "Gabriel",
                "Perez",
                "Quispe",
                "gabriel.perez@example.com",
                "https://example.com/photos/user-1.jpg",
                "ROLE_USER",
                null
            );
            when(userService.getActualUser()).thenReturn(user);
            when(userMapper.toMeDto(user)).thenReturn(dto);

            ResponseEntity<UserMeDTO> response = userController.getCurrentUser();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(1L);
            assertThat(response.getBody().names()).isEqualTo("Gabriel");
            assertThat(response.getBody().firstLastName()).isEqualTo("Perez");
            assertThat(response.getBody().secondLastName()).isEqualTo("Quispe");
            assertThat(response.getBody().email()).isEqualTo("gabriel.perez@example.com");
            assertThat(response.getBody().photoUrl()).isEqualTo("https://example.com/photos/user-1.jpg");
        }

        @Test
        @DisplayName("200 – campos opcionales pueden ser null")
        void shouldHandleNullOptionalFields() {
            User user = User.builder()
                .id(2L)
                .names("Ana")
                .firstLastName("Lopez")
                .secondLastName(null)
                .email("ana.lopez@example.com")
                .photoUrl(null)
                .role("ROLE_USER")
                .isActive(true)
                .build();
            UserMeDTO dto = new UserMeDTO(
                2L,
                "Ana",
                "Lopez",
                null,
                "ana.lopez@example.com",
                null,
                "ROLE_USER",
                null
            );
            when(userService.getActualUser()).thenReturn(user);
            when(userMapper.toMeDto(user)).thenReturn(dto);

            ResponseEntity<UserMeDTO> response = userController.getCurrentUser();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().secondLastName()).isNull();
            assertThat(response.getBody().photoUrl()).isNull();
        }

        @Test
        @DisplayName("delega correctamente al servicio")
        void shouldDelegateToUserService() {
            User user = buildUser();
            when(userService.getActualUser()).thenReturn(user);
            when(userMapper.toMeDto(user)).thenReturn(new UserMeDTO(
                1L,
                "Gabriel",
                "Perez",
                "Quispe",
                "gabriel.perez@example.com",
                "https://example.com/photos/user-1.jpg",
                "ROLE_USER",
                null
            ));

            userController.getCurrentUser();

            verify(userService, times(1)).getActualUser();
            verify(userMapper, times(1)).toMeDto(user);
        }
    }
}
