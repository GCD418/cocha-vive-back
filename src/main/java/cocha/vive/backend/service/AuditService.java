package cocha.vive.backend.service;

import cocha.vive.backend.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class AuditService {

    public Long getActualUserId() {
        User actualUser = getActualUser();
        log.debug("Retrieved authenticated user id: {}", actualUser.getId());
        return actualUser.getId();
    }

    public User getActualUser() {
        log.debug("Resolving authenticated user from security context");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
            && authentication.isAuthenticated()
            && !Objects.equals(authentication.getPrincipal(), "anonymousUser")) {
            User authenticatedUser = (User) authentication.getPrincipal();
            log.debug("Authenticated user resolved with id: {}", authenticatedUser.getId());
            return authenticatedUser;
        }
        log.warn("No authenticated user found in security context");
        throw new RuntimeException("There is no authenticated user. Get Out of here");
    }
}
