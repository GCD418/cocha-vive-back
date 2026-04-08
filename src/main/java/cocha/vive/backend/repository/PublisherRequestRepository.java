package cocha.vive.backend.repository;

import cocha.vive.backend.model.PublisherRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRequestRepository extends JpaRepository<PublisherRequest, Long> {
}
