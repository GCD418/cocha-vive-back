package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.RequestStatus;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.PublisherRequestCreateDTO;
import cocha.vive.backend.model.mapper.PublisherRequestMapper;
import cocha.vive.backend.repository.PublisherRequestRepository;
import cocha.vive.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherRequestService {

	private static final String ROLE_PUBLISHER = "ROLE_PUBLISHER";

	private final PublisherRequestRepository publisherRequestRepository;
	private final PublisherRequestMapper publisherRequestMapper;
	private final CloudinaryService cloudinaryService;
	private final AuditService auditService;
	private final UserRepository userRepository;

	public List<PublisherRequest> getAll() {
		log.debug("Retrieving all publisher requests ordered by createdAt ASC");
		List<PublisherRequest> requests = publisherRequestRepository.findAllByOrderByCreatedAtAsc();
		log.debug("Retrieved {} publisher request(s) ordered by createdAt ASC", requests.size());
		return requests;
	}

	public List<PublisherRequest> getAllPending() {
		log.debug("Retrieving pending publisher requests ordered by createdAt ASC");
		List<PublisherRequest> requests = publisherRequestRepository
			.findByRequestStatusOrderByCreatedAtAsc(RequestStatus.PENDING);
		log.debug("Retrieved {} pending publisher request(s) ordered by createdAt ASC", requests.size());
		return requests;
	}

	public PublisherRequest getById(Long requestId) {
		log.debug("Searching publisher request with id: {}", requestId);
		PublisherRequest request = publisherRequestRepository.findById(requestId)
			.orElseThrow(() -> {
				log.warn("Publisher request not found with id: {}", requestId);
				return new ResourceNotFoundException("Publisher request not found with id: " + requestId);
			});
		log.debug("Found publisher request with id: {}", request.getId());
		return request;
	}

	@Transactional
	public PublisherRequest createRequest(PublisherRequestCreateDTO dto, List<MultipartFile> images) {
		Long actualUserId = auditService.getActualUserId();
		log.info("Creating publisher request for user id: {}", actualUserId);

		PublisherRequest publisherRequest = publisherRequestMapper.toEntity(dto);
		publisherRequest.setEvidenceImages(cloudinaryService.uploadImages(images));
		publisherRequest.setCreatedByUserId(actualUserId);
		// publisherRequest.setRequestStatus(RequestStatus.PENDING);
		// publisherRequest.setIsActive(true);

		PublisherRequest savedRequest = publisherRequestRepository.save(publisherRequest);
		log.info("Publisher request created with id: {} by user id: {}", savedRequest.getId(), actualUserId);
		return savedRequest;
	}

	@Transactional
	public PublisherRequest rejectRequest(Long requestId) {
		Long actualUserId = auditService.getActualUserId();
		log.info("Rejecting publisher request with id: {} by admin id: {}", requestId, actualUserId);

		PublisherRequest request = getById(requestId);
		request.setRequestStatus(RequestStatus.REJECTED);
		request.setModifiedByUserId(actualUserId);

		PublisherRequest savedRequest = publisherRequestRepository.save(request);
		log.info("Publisher request rejected with id: {}", savedRequest.getId());
		return savedRequest;
	}

	@Transactional
	public PublisherRequest approveRequest(Long requestId) {
		Long actualUserId = auditService.getActualUserId();
		log.info("Approving publisher request with id: {} by admin id: {}", requestId, actualUserId);

		PublisherRequest request = getById(requestId);
		User requestOwner = userRepository.findById(request.getCreatedByUserId())
			.orElseThrow(() -> {
				log.warn("Request owner user not found with id: {}", request.getCreatedByUserId());
				return new ResourceNotFoundException("User not found with id: " + request.getCreatedByUserId());
			});

		requestOwner.setRole(ROLE_PUBLISHER);
		userRepository.save(requestOwner);

		request.setRequestStatus(RequestStatus.APPROVED);
		request.setModifiedByUserId(actualUserId);

		PublisherRequest savedRequest = publisherRequestRepository.save(request);
		log.info("Publisher request approved with id: {} and user id: {} promoted to {}",
			savedRequest.getId(), requestOwner.getId(), ROLE_PUBLISHER);
		return savedRequest;
	}
}
