package cocha.vive.backend.repository;

import cocha.vive.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.QueryHints;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @QueryHints(@QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true"))
    Optional<User> findByEmail(String email);

    @QueryHints(@QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true"))
    List<User> findAllByRole(String role);
  
    Optional<User> findByFacebookProviderId(String facebookProviderId);

    Optional<User> findByFacebookPageId(String facebookPageId);

}
