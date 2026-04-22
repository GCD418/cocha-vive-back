package cocha.vive.backend.service;

import cocha.vive.backend.core.enums.AppFeature;
import cocha.vive.backend.exception.InvalidStateTransitionException;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.RequestStatus;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.Notification;
import cocha.vive.backend.model.dto.PublisherRequestCreateDTO;
import cocha.vive.backend.model.dto.PublisherRequestRejectDTO;
import cocha.vive.backend.model.dto.PublisherRequestResponseDTO;
import cocha.vive.backend.model.mapper.PublisherRequestMapper;
import cocha.vive.backend.repository.PublisherRequestRepository;
import cocha.vive.backend.repository.NotificationRepository;
import cocha.vive.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.Optional;
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
    private final UserService userService;
    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;
    private final NotificationRepository notificationRepository;

        public List<PublisherRequestResponseDTO> getAll() {
        log.debug("Retrieving all publisher requests ordered by createdAt ASC");
        List<PublisherRequest> requests = publisherRequestRepository.findAllByOrderByCreatedAtAsc();
        log.debug("Retrieved {} publisher request(s) ordered by createdAt ASC", requests.size());
        return publisherRequestMapper.toResponseDtoList(requests);
    }

    public List<PublisherRequestResponseDTO> getAllPending() {
        log.debug("Retrieving pending publisher requests ordered by createdAt ASC");
        List<PublisherRequest> requests = publisherRequestRepository
            .findByRequestStatusOrderByCreatedAtAsc(RequestStatus.PENDING);
        log.debug("Retrieved {} pending publisher request(s) ordered by createdAt ASC", requests.size());
        return publisherRequestMapper.toResponseDtoList(requests);
    }

    public PublisherRequestResponseDTO getById(Long requestId) {
        log.debug("Searching publisher request with id: {}", requestId);
        PublisherRequest request = publisherRequestRepository.findById(requestId)
            .orElseThrow(() -> {
                log.warn("Publisher request not found with id: {}", requestId);
                return new ResourceNotFoundException("Publisher request not found with id: " + requestId);
            });
        log.debug("Found publisher request with id: {}", request.getId());
        return publisherRequestMapper.toResponseDto(request);
    }

    public PublisherRequestResponseDTO getMyRequest() {
        Long actualUserId = auditService.getActualUserId();
        log.debug("Retrieving publisher request for user id: {}", actualUserId);
        PublisherRequest request = publisherRequestRepository
            .findByCreatedByUserIdIdAndIsActiveTrue(actualUserId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No publisher request found for user id: " + actualUserId));
        return publisherRequestMapper.toResponseDto(request);
    }

    private PublisherRequest getEntityById(Long requestId) {
        return publisherRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Publisher request not found with id: " + requestId));
    }

    @Transactional
    public PublisherRequestResponseDTO createRequest(PublisherRequestCreateDTO dto, List<MultipartFile> images) {
        Long actualUserId = auditService.getActualUserId();
        log.info("Creating publisher request for user id: {}", actualUserId);

        User actualUser = userRepository.findById(actualUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + actualUserId));

        Optional<PublisherRequest> existingRequest = publisherRequestRepository
            .findByCreatedByUserIdIdAndIsActiveTrue(actualUserId);

        if (existingRequest.isPresent()) {
            PublisherRequest previous = existingRequest.get();
            LocalDateTime oneDayAfterCreation = previous.getCreatedAt().plusDays(1);

            if (LocalDateTime.now().isBefore(oneDayAfterCreation)) {
                log.warn("User id: {} tried to create a new request before 1 day cooldown", actualUserId);
                throw new IllegalStateException(
                    "You must wait 1 day before submitting a new publisher request"
                );
            }

            previous.setIsActive(false);
            publisherRequestRepository.save(previous);
            log.info("Previous publisher request id: {} deactivated for user id: {}", previous.getId(), actualUserId);
        }

        PublisherRequest publisherRequest = publisherRequestMapper.toEntity(dto);
        publisherRequest.setEvidenceImages(cloudinaryService.uploadImages(images));
        publisherRequest.setCreatedByUserId(actualUser);
        PublisherRequest savedRequest = publisherRequestRepository.save(publisherRequest);
        log.info("Publisher request created with id: {} by user id: {}", savedRequest.getId(), actualUserId);

        if (featureToggleService.isEnabled(AppFeature.SEND_NEW_PUBLISHER_REQUEST_NOTIFICATION_EMAIL.getUnleashKey())) {
            userService.getAllAdmins().forEach(admin ->
                emailService.sendNewConvertToPublisherRequestEmail(admin, savedRequest)
            );
        }

        return publisherRequestMapper.toResponseDto(savedRequest);
    }

    @Transactional
    public PublisherRequestResponseDTO rejectRequest(Long requestId,  PublisherRequestRejectDTO dto) {
        Long actualUserId = auditService.getActualUserId();
        log.info("Rejecting publisher request with id: {} by admin id: {}", requestId, actualUserId);

        PublisherRequest request = getEntityById(requestId);

        if (request.getRequestStatus() != RequestStatus.PENDING) {
            log.warn("Cannot reject publisher request id: {} with status: {}", requestId, request.getRequestStatus());
            throw new InvalidStateTransitionException(
                "Only PENDING requests can be rejected. Current status: " + request.getRequestStatus()
            );
        }

        request.setRequestStatus(RequestStatus.REJECTED);
        request.setRejectionReason(dto.getRejectionReason());
        request.setModifiedByUserId(actualUserId);

        PublisherRequest savedRequest = publisherRequestRepository.save(request);
        log.info("Publisher request rejected with id: {}", savedRequest.getId());

        User affectedUser = savedRequest.getCreatedByUserId();
        createNotification(
            affectedUser,
            "Solicitud rechazada",
            savedRequest.getRejectionReason()
        );
        emailService.sendPublisherRequestRejectedEmail(affectedUser, savedRequest);

        return publisherRequestMapper.toResponseDto(savedRequest);
    }

    @Transactional
    public PublisherRequestResponseDTO approveRequest(Long requestId) {
        Long actualUserId = auditService.getActualUserId();
        log.info("Approving publisher request with id: {} by admin id: {}", requestId, actualUserId);

        PublisherRequest request = getEntityById(requestId);
        User requestOwner = userRepository.findById(request.getCreatedByUserId().getId())
            .orElseThrow(() -> {
                log.warn("Request owner user not found with id: {}", request.getCreatedByUserId().getId());
                return new ResourceNotFoundException("User not found with id: " + request.getCreatedByUserId().getId());
            });

        requestOwner.setRole(ROLE_PUBLISHER);
        userRepository.save(requestOwner);

        request.setRequestStatus(RequestStatus.APPROVED);
        request.setModifiedByUserId(actualUserId);

        PublisherRequest savedRequest = publisherRequestRepository.save(request);
        log.info("Publisher request approved with id: {} and user id: {} promoted to {}",
            savedRequest.getId(), requestOwner.getId(), ROLE_PUBLISHER);

        createNotification(
            requestOwner,
            "Solicitud aprobada",
            "Now you're a Publisher"
        );
        emailService.sendPublisherRequestApprovedEmail(requestOwner, savedRequest);

        return publisherRequestMapper.toResponseDto(savedRequest);
    }

    private void createNotification(User recipient, String title, String shortDescription) {
        Notification notification = Notification.builder()
            .notifiedUser(recipient)
            .title(title)
            .shortDescription(shortDescription)
            .build();
        notificationRepository.save(notification);
    }
}
