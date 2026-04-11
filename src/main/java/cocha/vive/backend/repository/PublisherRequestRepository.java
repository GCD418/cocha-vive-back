package cocha.vive.backend.repository;

import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.RequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublisherRequestRepository extends JpaRepository<PublisherRequest, Long> {

    @EntityGraph(attributePaths = "createdByUserId")
    List<PublisherRequest> findAllByOrderByCreatedAtAsc();

    @EntityGraph(attributePaths = "createdByUserId")
    List<PublisherRequest> findByRequestStatusOrderByCreatedAtAsc(RequestStatus requestStatus);

    @EntityGraph(attributePaths = "createdByUserId")
    Optional<PublisherRequest> findById(Long id);

    @EntityGraph(attributePaths = "createdByUserId")
    Optional<PublisherRequest> findByCreatedByUserIdIdAndIsActiveTrue(Long userId);
}
