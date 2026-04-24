package cocha.vive.backend.service;

import cocha.vive.backend.exception.InvalidStateTransitionException;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.RequestStatus;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.PublisherRequestRejectDTO;
import cocha.vive.backend.model.dto.PublisherRequestResponseDTO;
import cocha.vive.backend.model.mapper.PublisherRequestMapper;
import cocha.vive.backend.repository.PublisherRequestRepository;
import cocha.vive.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublisherRequestService Tests")
public class PublisherRequestServiceTest {

    @Mock
    private PublisherRequestRepository publisherRequestRepository;
    @Mock
    private PublisherRequestMapper publisherRequestMapper;
    @Mock
    private AuditService auditService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private EmailService emailService;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private PublisherRequestUserNotificationFacade publisherRequestUserNotificationFacade;

    @InjectMocks
    private PublisherRequestService publisherRequestService;

    // ─── Fixtures ───────────────────────────────────────────────────────────────

    private User buildUser() {
        return User.builder()
            .id(1L)
            .names("Gabriel")
            .firstLastName("Perez")
            .email("gabriel.perez@example.com")
            .role("ROLE_USER")
            .isActive(true)
            .build();
    }

    private PublisherRequest buildRequest(RequestStatus status) {
        return PublisherRequest.builder()
            .id(1L)
            .requestReason("We organize community cultural events weekly.")
            .legalEntityName("Cocha Cultura SRL")
            .evidenceImages(List.of("https://example.com/evidence1.jpg"))
            .createdByUserId(buildUser())
            .requestStatus(status)
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .build();
    }

    private PublisherRequestRejectDTO buildRejectDTO() {
        return new PublisherRequestRejectDTO(
            "The provided legal entity documentation is incomplete or invalid."
        );
    }

    private PublisherRequestResponseDTO buildResponseDTO(RequestStatus status, String rejectionReason) {
        return new PublisherRequestResponseDTO(
            1L,
            "We organize community cultural events weekly.",
            "Cocha Cultura SRL",
            rejectionReason,
            List.of("https://example.com/evidence1.jpg"),
            null,
            status,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true,
            null
        );
    }

    // ─── rejectRequest ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("rejectRequest")
    class RejectRequest {

        @Test
        @DisplayName("rechaza solicitud PENDING y persiste el motivo correctamente")
        void shouldRejectPendingRequestAndPersistReason() {
            PublisherRequest request = buildRequest(RequestStatus.PENDING);
            PublisherRequestRejectDTO dto = buildRejectDTO();
            PublisherRequestResponseDTO responseDTO = buildResponseDTO(
                RequestStatus.REJECTED, dto.getRejectionReason()
            );

            when(auditService.getActualUserId()).thenReturn(99L);
            when(publisherRequestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(publisherRequestRepository.save(any(PublisherRequest.class))).thenReturn(request);
            when(publisherRequestMapper.toResponseDto(any(PublisherRequest.class))).thenReturn(responseDTO);

            PublisherRequestResponseDTO result = publisherRequestService.rejectRequest(1L, dto);

            assertThat(result.getRequestStatus()).isEqualTo(RequestStatus.REJECTED);
            assertThat(result.getRejectionReason())
                .isEqualTo("The provided legal entity documentation is incomplete or invalid.");
            verify(publisherRequestRepository, times(1)).save(any(PublisherRequest.class));
            verify(publisherRequestUserNotificationFacade).notifyRejected(request.getCreatedByUserId(), request);
        }

        @Test
        @DisplayName("persiste el motivo de rechazo exacto en la entidad")
        void shouldPersistExactRejectionReasonInEntity() {
            PublisherRequest request = buildRequest(RequestStatus.PENDING);
            PublisherRequestRejectDTO dto = buildRejectDTO();

            when(auditService.getActualUserId()).thenReturn(99L);
            when(publisherRequestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(publisherRequestRepository.save(any(PublisherRequest.class))).thenReturn(request);
            when(publisherRequestMapper.toResponseDto(any())).thenReturn(
                buildResponseDTO(RequestStatus.REJECTED, dto.getRejectionReason())
            );

            publisherRequestService.rejectRequest(1L, dto);

            ArgumentCaptor<PublisherRequest> captor = ArgumentCaptor.forClass(PublisherRequest.class);
            verify(publisherRequestRepository).save(captor.capture());
            assertThat(captor.getValue().getRejectionReason())
                .isEqualTo("The provided legal entity documentation is incomplete or invalid.");
            assertThat(captor.getValue().getRequestStatus()).isEqualTo(RequestStatus.REJECTED);
            assertThat(captor.getValue().getModifiedByUserId()).isEqualTo(99L);
            verify(publisherRequestUserNotificationFacade).notifyRejected(request.getCreatedByUserId(), request);
        }

        @Test
        @DisplayName("lanza InvalidStateTransitionException si la solicitud no está en PENDING")
        void shouldThrowWhenRequestIsNotPending() {
            PublisherRequest request = buildRequest(RequestStatus.APPROVED);
            PublisherRequestRejectDTO dto = buildRejectDTO();

            when(auditService.getActualUserId()).thenReturn(99L);
            when(publisherRequestRepository.findById(1L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> publisherRequestService.rejectRequest(1L, dto))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("APPROVED");

            verify(publisherRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza InvalidStateTransitionException si la solicitud ya está REJECTED")
        void shouldThrowWhenRequestIsAlreadyRejected() {
            PublisherRequest request = buildRequest(RequestStatus.REJECTED);
            PublisherRequestRejectDTO dto = buildRejectDTO();

            when(auditService.getActualUserId()).thenReturn(99L);
            when(publisherRequestRepository.findById(1L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> publisherRequestService.rejectRequest(1L, dto))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("REJECTED");

            verify(publisherRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException si la solicitud no existe")
        void shouldThrowWhenRequestNotFound() {
            when(auditService.getActualUserId()).thenReturn(99L);
            when(publisherRequestRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publisherRequestService.rejectRequest(99L, buildRejectDTO()))
                .isInstanceOf(ResourceNotFoundException.class);

            verify(publisherRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("approveRequest")
    class ApproveRequest {

        @Test
        @DisplayName("aprueba solicitud PENDING, promueve usuario y notifica por app/email")
        void shouldApprovePendingRequestPromoteUserAndNotify() {
            User owner = buildUser();
            owner.setRole("ROLE_USER");
            PublisherRequest request = buildRequest(RequestStatus.PENDING);
            request.setCreatedByUserId(owner);

            PublisherRequestResponseDTO responseDTO = buildResponseDTO(RequestStatus.APPROVED, null);

            when(auditService.getActualUserId()).thenReturn(99L);
            when(publisherRequestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(publisherRequestRepository.save(any(PublisherRequest.class))).thenAnswer(i -> i.getArgument(0));
            when(publisherRequestMapper.toResponseDto(any(PublisherRequest.class))).thenReturn(responseDTO);

            PublisherRequestResponseDTO result = publisherRequestService.approveRequest(1L);

            assertThat(result.getRequestStatus()).isEqualTo(RequestStatus.APPROVED);
            assertThat(owner.getRole()).isEqualTo("ROLE_PUBLISHER");
            verify(publisherRequestUserNotificationFacade).notifyApproved(owner, request);
        }
    }
}
