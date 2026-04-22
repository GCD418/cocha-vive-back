package cocha.vive.backend.service;

import cocha.vive.backend.exception.InvalidRoleTransitionException;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.RoleChangeResponseDTO;
import cocha.vive.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminRoleService Tests")
public class AdminRoleServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AdminRoleService adminRoleService;

    private User buildUser(Long id, String role) {
        return User.builder()
            .id(id)
            .email("user" + id + "@mail.com")
            .role(role)
            .isActive(true)
            .build();
    }

    @Nested
    @DisplayName("promoteToAdmin")
    class PromoteToAdmin {

        @Test
        @DisplayName("promotes ROLE_USER to ROLE_ADMIN successfully")
        void shouldPromoteUserToAdmin() {
            User target = buildUser(2L, "ROLE_USER");
            when(auditService.getActualUserId()).thenReturn(1L);
            when(userRepository.findById(2L)).thenReturn(Optional.of(target));
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            RoleChangeResponseDTO result = adminRoleService.promoteToAdmin(2L);

            assertThat(result.role()).isEqualTo("ROLE_ADMIN");
            assertThat(result.id()).isEqualTo(2L);
            assertThat(target.getModifiedByUserId()).isEqualTo(1L);
            verify(userRepository).save(target);
        }

        @Test
        @DisplayName("throws when target is already ROLE_ADMIN")
        void shouldThrowWhenAlreadyAdmin() {
            User target = buildUser(2L, "ROLE_ADMIN");
            when(auditService.getActualUserId()).thenReturn(1L);
            when(userRepository.findById(2L)).thenReturn(Optional.of(target));

            assertThatThrownBy(() -> adminRoleService.promoteToAdmin(2L))
                .isInstanceOf(InvalidRoleTransitionException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when target is ROLE_SUPERADMIN")
        void shouldThrowWhenTargetIsSuperadmin() {
            User target = buildUser(2L, "ROLE_SUPERADMIN");
            when(auditService.getActualUserId()).thenReturn(1L);
            when(userRepository.findById(2L)).thenReturn(Optional.of(target));

            assertThatThrownBy(() -> adminRoleService.promoteToAdmin(2L))
                .isInstanceOf(InvalidRoleTransitionException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(auditService.getActualUserId()).thenReturn(1L);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminRoleService.promoteToAdmin(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("demoteToUser")
    class DemoteToUser {

        @Test
        @DisplayName("demotes ROLE_ADMIN to ROLE_USER successfully")
        void shouldDemoteAdminToUser() {
            User target = buildUser(2L, "ROLE_ADMIN");
            when(auditService.getActualUserId()).thenReturn(1L);
            when(userRepository.findById(2L)).thenReturn(Optional.of(target));
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            RoleChangeResponseDTO result = adminRoleService.demoteToUser(2L);

            assertThat(result.role()).isEqualTo("ROLE_USER");
            assertThat(target.getModifiedByUserId()).isEqualTo(1L);
            verify(userRepository).save(target);
        }

        @Test
        @DisplayName("throws when actor tries to demote themselves")
        void shouldThrowOnSelfDemotion() {
            when(auditService.getActualUserId()).thenReturn(1L);

            assertThatThrownBy(() -> adminRoleService.demoteToUser(1L))
                .isInstanceOf(InvalidRoleTransitionException.class)
                .hasMessageContaining("Self-demotion");

            verify(userRepository, never()).findById(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when target is already ROLE_USER")
        void shouldThrowWhenAlreadyUser() {
            User target = buildUser(2L, "ROLE_USER");
            when(auditService.getActualUserId()).thenReturn(1L);
            when(userRepository.findById(2L)).thenReturn(Optional.of(target));

            assertThatThrownBy(() -> adminRoleService.demoteToUser(2L))
                .isInstanceOf(InvalidRoleTransitionException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(auditService.getActualUserId()).thenReturn(1L);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminRoleService.demoteToUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
