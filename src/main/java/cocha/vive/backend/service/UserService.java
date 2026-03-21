package cocha.vive.backend.service;

import cocha.vive.backend.model.User;
import cocha.vive.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
}
