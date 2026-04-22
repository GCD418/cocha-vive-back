package cocha.vive.backend.controller;

import cocha.vive.backend.exception.InvalidRoleTransitionException;
import cocha.vive.backend.model.dto.RoleChangeResponseDTO;
import cocha.vive.backend.model.mapper.UserMapper;
import cocha.vive.backend.service.AdminRoleService;
import cocha.vive.backend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminRoleController Tests")
class AdminRoleControllerTest {

    @Mock
    private AdminRoleService adminRoleService;

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminRoleController adminRoleController;

    private RoleChangeResponseDTO buildResponse(Long id, String email, String role) {
        return new RoleChangeResponseDTO(id, email, role);
    }

    // ─── PATCH /{userId}/promote ─────────────────────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/admin/users/{userId}/promote")
    class PromoteToAdmin {

        @Test
        @DisplayName("200 – promover usuario a ADMIN exitosamente")
        void shouldPromoteUserToAdmin() {
            RoleChangeResponseDTO response = buildResponse(1L, "user@mail.com", "ROLE_ADMIN");
            when(adminRoleService.promoteToAdmin(1L)).thenReturn(response);

            ResponseEntity<RoleChangeResponseDTO> result = adminRoleController.promoteToAdmin(1L);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().id()).isEqualTo(1L);
            assertThat(result.getBody().role()).isEqualTo("ROLE_ADMIN");
            verify(adminRoleService, times(1)).promoteToAdmin(1L);
        }

        @Test
        @DisplayName("delega al servicio con el userId correcto")
        void shouldDelegateToServiceWithCorrectUserId() {
            when(adminRoleService.promoteToAdmin(42L))
                .thenReturn(buildResponse(42L, "user@mail.com", "ROLE_ADMIN"));

            adminRoleController.promoteToAdmin(42L);

            verify(adminRoleService).promoteToAdmin(42L);
        }

        @Test
        @DisplayName("lanza InvalidRoleTransitionException si usuario ya es ADMIN")
        void shouldThrowWhenAlreadyAdmin() {
            when(adminRoleService.promoteToAdmin(1L))
                .thenThrow(new InvalidRoleTransitionException("User is already ADMIN"));

            assertThatThrownBy(() -> adminRoleController.promoteToAdmin(1L))
                .isInstanceOf(InvalidRoleTransitionException.class)
                .hasMessageContaining("already ADMIN");

            verify(adminRoleService, times(1)).promoteToAdmin(1L);
        }
    }

    // ─── PATCH /{userId}/demote ──────────────────────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/admin/users/{userId}/demote")
    class DemoteToUser {

        @Test
        @DisplayName("200 – degradar usuario a USER exitosamente")
        void shouldDemoteAdminToUser() {
            RoleChangeResponseDTO response = buildResponse(1L, "user@mail.com", "ROLE_USER");
            when(adminRoleService.demoteToUser(1L)).thenReturn(response);

            ResponseEntity<RoleChangeResponseDTO> result = adminRoleController.demoteToUser(1L);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().id()).isEqualTo(1L);
            assertThat(result.getBody().role()).isEqualTo("ROLE_USER");
            verify(adminRoleService, times(1)).demoteToUser(1L);
        }

        @Test
        @DisplayName("delega al servicio con el userId correcto")
        void shouldDelegateToServiceWithCorrectUserId() {
            when(adminRoleService.demoteToUser(42L))
                .thenReturn(buildResponse(42L, "user@mail.com", "ROLE_USER"));

            adminRoleController.demoteToUser(42L);

            verify(adminRoleService).demoteToUser(42L);
        }

        @Test
        @DisplayName("lanza InvalidRoleTransitionException si usuario ya es USER")
        void shouldThrowWhenAlreadyUser() {
            when(adminRoleService.demoteToUser(1L))
                .thenThrow(new InvalidRoleTransitionException("User is already USER"));

            assertThatThrownBy(() -> adminRoleController.demoteToUser(1L))
                .isInstanceOf(InvalidRoleTransitionException.class)
                .hasMessageContaining("already USER");

            verify(adminRoleService, times(1)).demoteToUser(1L);
        }
    }
}
