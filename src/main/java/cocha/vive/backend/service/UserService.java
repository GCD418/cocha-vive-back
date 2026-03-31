package cocha.vive.backend.service;

import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.UserCreateDTO;
import cocha.vive.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuditService auditService;

    public List<User> getAll() {
        log.debug("Retrieving all users");
        List<User> users = userRepository.findAll();
        log.debug("Retrieved {} users", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public Optional<User> getByEmail(String email) {
        log.debug("Searching user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            log.debug("Found user with id: {} for email: {}", user.get().getId(), email);
        } else {
            log.debug("User not found for email: {}", email);
        }
        return user;
    }

    @Transactional
    public User create(UserCreateDTO newUser) {
        log.info("Creating user with email: {}", newUser.getEmail());
        User savedUser = userRepository.save(User.builder()
            .email(newUser.getEmail())
            .names(newUser.getName())
            .firstLastName(newUser.getFirstLastName())
            .documentNumber(newUser.getDocumentNumber())
            .photoUrl(newUser.getPhotoUrl())
            .role(newUser.getRole())
            .googleProviderId(newUser.getGoogleProviderId())
            .build()
        );
        log.info("User created with id: {} and email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @Transactional
    public void updateDocumentNumber(String email, String documentNumber, String extension) {
        log.info("Updating document data for user with email: {}", email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("User not found for document update, email: {}", email);
                return new UsernameNotFoundException("Not Found User");
            });
        user.setDocumentNumber(documentNumber);
        user.setDocumentExtension(extension);

        userRepository.save(user);
        log.info("Updated document data for user id: {}", user.getId());
    }

    public User getActualUser() {
        User actualUser = auditService.getActualUser();
        log.debug("Retrieved actual user with id: {}", actualUser.getId());
        return actualUser;
    }
}
