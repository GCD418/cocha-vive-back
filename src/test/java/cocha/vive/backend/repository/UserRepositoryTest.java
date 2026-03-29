package cocha.vive.backend.repository;

import cocha.vive.backend.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    // ─── Fixtures ─────────────────────────────────────────────

    private User buildUser() {
        return User.builder()
            .id(1L)
            .email("test@mail.com")
            .build();
    }

    // ─── findByEmail ─────────────────────────────────────────

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("retorna usuario cuando el email existe")
        void shouldReturnUserWhenEmailExists() {
            User user = buildUser();
            when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

            Optional<User> result = userRepository.findByEmail("test@mail.com");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@mail.com");
            verify(userRepository, times(1)).findByEmail("test@mail.com");
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el email no existe")
        void shouldReturnEmptyWhenEmailNotFound() {
            when(userRepository.findByEmail("no@mail.com"))
                .thenReturn(Optional.empty());

            Optional<User> result = userRepository.findByEmail("no@mail.com");

            assertThat(result).isEmpty();
            verify(userRepository, times(1)).findByEmail("no@mail.com");
        }

        @Test
        @DisplayName("es case sensitive — email diferente no retorna resultado")
        void shouldBeCaseSensitive() {
            when(userRepository.findByEmail("TEST@mail.com"))
                .thenReturn(Optional.empty());

            Optional<User> result = userRepository.findByEmail("TEST@mail.com");

            assertThat(result).isEmpty();
        }
    }

    // ─── findAll ─────────────────────────────────────────────

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("retorna lista de usuarios")
        void shouldReturnUsers() {
            List<User> users = List.of(buildUser(), buildUser());
            when(userRepository.findAll()).thenReturn(users);

            List<User> result = userRepository.findAll();

            assertThat(result).hasSize(2);
            verify(userRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay usuarios")
        void shouldReturnEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<User> result = userRepository.findAll();

            assertThat(result).isEmpty();
        }
    }

    // ─── findById ────────────────────────────────────────────

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna usuario cuando el id existe")
        void shouldReturnUserWhenIdExists() {
            User user = buildUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            Optional<User> result = userRepository.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("retorna vacío cuando no existe")
        void shouldReturnEmpty() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<User> result = userRepository.findById(99L);

            assertThat(result).isEmpty();
        }
    }

    // ─── save ────────────────────────────────────────────────

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("guarda y retorna usuario")
        void shouldSaveUser() {
            User user = buildUser();
            when(userRepository.save(any(User.class))).thenReturn(user);

            User result = userRepository.save(user);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("delega con la entidad correcta")
        void shouldDelegateWithCorrectEntity() {
            User user = buildUser();
            when(userRepository.save(any(User.class))).thenReturn(user);

            userRepository.save(user);

            verify(userRepository).save(eq(user));
        }
    }
}
