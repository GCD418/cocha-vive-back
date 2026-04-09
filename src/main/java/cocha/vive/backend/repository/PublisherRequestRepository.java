package cocha.vive.backend.repository;

import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublisherRequestRepository extends JpaRepository<PublisherRequest, Long> {

	List<PublisherRequest> findAllByOrderByCreatedAtAsc();

	List<PublisherRequest> findByRequestStatusOrderByCreatedAtAsc(RequestStatus requestStatus);
}
