package cocha.vive.backend.service;

import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.UserCreateDTO;
import cocha.vive.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnAllUsers() {
        List<User> users = List.of(new User(), new User());

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAll();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void shouldReturnUserByEmail() {
        User user = new User();
        user.setEmail("test@mail.com");

        when(userRepository.findByEmail("test@mail.com"))
            .thenReturn(Optional.of(user));

        Optional<User> result = userService.getByEmail("test@mail.com");

        assertTrue(result.isPresent());
        assertEquals("test@mail.com", result.get().getEmail());
    }

    @Test
    void shouldCreateUser() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail("test@mail.com");
        dto.setName("Diego");
        dto.setFirstLastName("Rios");

        when(userRepository.save(any(User.class)))
            .thenAnswer(i -> i.getArgument(0));

        User result = userService.create(dto);

        assertNotNull(result);
        assertEquals("test@mail.com", result.getEmail());
        assertEquals("Diego", result.getNames());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateDocumentNumber() {
        User user = new User();
        user.setEmail("test@mail.com");

        when(userRepository.findByEmail("test@mail.com"))
            .thenReturn(Optional.of(user));

        userService.updateDocumentNumber("test@mail.com", "123456", "LP");

        assertEquals("123456", user.getDocumentNumber());
        assertEquals("LP", user.getDocumentExtension());

        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("test@mail.com"))
            .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
            userService.updateDocumentNumber("test@mail.com", "123", "CB")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnActualUser() {
        User user = new User();
        user.setId(99L);

        when(auditService.getActualUser()).thenReturn(user);

        User result = userService.getActualUser();

        assertNotNull(result);
        assertEquals(99L, result.getId()); // 🔥 mejora clave
        verify(auditService).getActualUser();
    }
}
