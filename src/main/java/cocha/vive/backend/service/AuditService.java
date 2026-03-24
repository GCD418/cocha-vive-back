package cocha.vive.backend.service;

import cocha.vive.backend.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuditService {

    public Long getActualUserId() {
        return getActualUser().getId();
    }

    public User getActualUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
            && authentication.isAuthenticated()
            && !Objects.equals(authentication.getPrincipal(), "anonymousUser")) {
            return (User) authentication.getPrincipal();
        }
        throw new RuntimeException("There is no authenticated user. Get Out of here");
    }
}
