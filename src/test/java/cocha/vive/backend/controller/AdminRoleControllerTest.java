package cocha.vive.backend.controller;

import cocha.vive.backend.exception.InvalidRoleTransitionException;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.dto.RoleChangeResponseDTO;
import cocha.vive.backend.model.dto.PublisherDemotionDTO;
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

    private PublisherDemotionDTO buildDemotionDTO() {
        return new PublisherDemotionDTO("Publisher repeatedly violated content guidelines.");
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

    @Nested
    @DisplayName("PATCH /api/admin/users/{userId}/demote-publisher")
    class DemotePublisherToUser {

        @Test
        @DisplayName("200 – demotes publisher to ROLE_USER successfully")
        void shouldDemotePublisherToUser() {
            PublisherDemotionDTO dto = buildDemotionDTO();
            RoleChangeResponseDTO response = buildResponse(1L, "publisher@mail.com", "ROLE_USER");
            when(adminRoleService.demotePublisherToUser(1L, dto)).thenReturn(response);

            ResponseEntity<RoleChangeResponseDTO> result =
                adminRoleController.demotePublisherToUser(1L, dto);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().id()).isEqualTo(1L);
            assertThat(result.getBody().role()).isEqualTo("ROLE_USER");
            verify(adminRoleService, times(1)).demotePublisherToUser(1L, dto);
        }

        @Test
        @DisplayName("delegates to service with correct userId and DTO")
        void shouldDelegateToServiceWithCorrectArgs() {
            PublisherDemotionDTO dto = buildDemotionDTO();
            when(adminRoleService.demotePublisherToUser(42L, dto))
                .thenReturn(buildResponse(42L, "publisher@mail.com", "ROLE_USER"));

            adminRoleController.demotePublisherToUser(42L, dto);

            verify(adminRoleService).demotePublisherToUser(42L, dto);
        }

        @Test
        @DisplayName("throws InvalidRoleTransitionException when target is not ROLE_PUBLISHER")
        void shouldThrowWhenTargetIsNotPublisher() {
            PublisherDemotionDTO dto = buildDemotionDTO();
            when(adminRoleService.demotePublisherToUser(1L, dto))
                .thenThrow(new InvalidRoleTransitionException(
                    "User id: 1 cannot be demoted. Current role: ROLE_USER"
                ));

            assertThatThrownBy(() -> adminRoleController.demotePublisherToUser(1L, dto))
                .isInstanceOf(InvalidRoleTransitionException.class)
                .hasMessageContaining("ROLE_USER");

            verify(adminRoleService, times(1)).demotePublisherToUser(1L, dto);
        }

        @Test
        @DisplayName("throws InvalidRoleTransitionException on self-demotion")
        void shouldThrowOnSelfDemotion() {
            PublisherDemotionDTO dto = buildDemotionDTO();
            when(adminRoleService.demotePublisherToUser(1L, dto))
                .thenThrow(new InvalidRoleTransitionException("Self-demotion is not allowed"));

            assertThatThrownBy(() -> adminRoleController.demotePublisherToUser(1L, dto))
                .isInstanceOf(InvalidRoleTransitionException.class)
                .hasMessageContaining("Self-demotion");

            verify(adminRoleService, times(1)).demotePublisherToUser(1L, dto);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            PublisherDemotionDTO dto = buildDemotionDTO();
            when(adminRoleService.demotePublisherToUser(99L, dto))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

            assertThatThrownBy(() -> adminRoleController.demotePublisherToUser(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

            verify(adminRoleService, times(1)).demotePublisherToUser(99L, dto);
        }
    }
}
