package cocha.vive.backend.service;

import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.PublisherRequestCreateDTO;
import cocha.vive.backend.model.dto.PublisherRequestResponseDTO;
import cocha.vive.backend.model.mapper.PublisherRequestMapper;
import cocha.vive.backend.repository.PublisherRequestRepository;
import cocha.vive.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublisherRequestService email integration")
class PublisherRequestServiceEmailIntegrationTest {

    @Mock
    private PublisherRequestRepository publisherRequestRepository;
    @Mock
    private PublisherRequestMapper publisherRequestMapper;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private AuditService auditService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private PublisherRequestService publisherRequestService;

    @Test
    @DisplayName("createRequest should notify all admins")
    void createRequest_shouldNotifyAllAdmins() {
        Long userId = 30L;
        User requester = new User();
        requester.setId(userId);
        requester.setEmail("requester@mail.com");

        User admin1 = new User();
        admin1.setId(1L);
        admin1.setEmail("admin1@mail.com");

        User admin2 = new User();
        admin2.setId(2L);
        admin2.setEmail("admin2@mail.com");

        PublisherRequestCreateDTO dto = new PublisherRequestCreateDTO("Motivo", "Entidad");
        PublisherRequest mapped = new PublisherRequest();
        PublisherRequest saved = new PublisherRequest();
        saved.setCreatedByUserId(requester);
        saved.setLegalEntityName("Entidad");
        saved.setRequestReason("Motivo");

        PublisherRequestResponseDTO responseDTO = new PublisherRequestResponseDTO(
            1L, "Motivo", "Entidad", List.of(), null, null, null, null, true, null
        );

        when(auditService.getActualUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(publisherRequestRepository.findByCreatedByUserIdIdAndIsActiveTrue(userId)).thenReturn(Optional.empty());
        when(publisherRequestMapper.toEntity(dto)).thenReturn(mapped);
        when(cloudinaryService.uploadImages(anyList())).thenReturn(List.of("img.jpg"));
        when(publisherRequestRepository.save(mapped)).thenReturn(saved);
        when(userService.getAllAdmins()).thenReturn(List.of(admin1, admin2));
        when(publisherRequestMapper.toResponseDto(saved)).thenReturn(responseDTO);

        PublisherRequestResponseDTO result = publisherRequestService.createRequest(dto, List.of());

        assertThat(result).isNotNull();
        verify(emailService).sendNewConvertToPublisherRequestEmail(admin1, saved);
        verify(emailService).sendNewConvertToPublisherRequestEmail(admin2, saved);
    }
}
