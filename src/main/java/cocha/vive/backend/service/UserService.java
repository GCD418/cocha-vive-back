package cocha.vive.backend.service;

import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.UserCreateDTO;
import cocha.vive.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User create(UserCreateDTO newUser) {
        return userRepository.save(User.builder()
            .email(newUser.getEmail())
            .names(newUser.getName())
            .firstLastName(newUser.getFirstLastName())
            .documentNumber(newUser.getDocumentNumber())
            .photoUrl(newUser.getPhotoUrl())
            .role(newUser.getRole())
            .googleProviderId(newUser.getGoogleProviderId())
            .build()
        );
    }

    @Transactional
    public void updateDocumentNumber(String email, String documentNumber, String extension) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Not Found User"));
        user.setDocumentNumber(documentNumber);
        user.setDocumentExtension(extension);

        userRepository.save(user);
    }
}
