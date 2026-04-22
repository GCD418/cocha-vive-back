package cocha.vive.backend.controller;

import cocha.vive.backend.model.RequestStatus;
import cocha.vive.backend.model.dto.PublisherRequestRejectDTO;
import cocha.vive.backend.model.dto.PublisherRequestResponseDTO;
import cocha.vive.backend.service.PublisherRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublisherRequestController Tests")
public class PublisherRequestControllerTest {

    @Mock
    private PublisherRequestService publisherRequestService;

    @InjectMocks
    private PublisherRequestController publisherRequestController;

    // ─── Fixtures ───────────────────────────────────────────────────────────────

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

    private PublisherRequestRejectDTO buildRejectDTO() {
        return new PublisherRequestRejectDTO(
            "The provided legal entity documentation is incomplete or invalid."
        );
    }

    // ─── PATCH /api/publisher-requests/{id}/reject ───────────────────────────────

    @Nested
    @DisplayName("PATCH /api/publisher-requests/{id}/reject")
    class RejectRequest {

        @Test
        @DisplayName("200 – rechaza solicitud correctamente y retorna motivo en respuesta")
        void shouldReturn200WhenRequestRejected() {
            PublisherRequestRejectDTO dto = buildRejectDTO();
            PublisherRequestResponseDTO response = buildResponseDTO(RequestStatus.REJECTED, dto.getRejectionReason());
            when(publisherRequestService.rejectRequest(1L, dto)).thenReturn(response);

            ResponseEntity<PublisherRequestResponseDTO> result =
                publisherRequestController.rejectRequest(1L, dto);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getRequestStatus()).isEqualTo(RequestStatus.REJECTED);
            assertThat(result.getBody().getRejectionReason())
                .isEqualTo("The provided legal entity documentation is incomplete or invalid.");
            verify(publisherRequestService, times(1)).rejectRequest(1L, dto);
        }

        @Test
        @DisplayName("delega al servicio con el id y DTO correctos")
        void shouldDelegateToServiceWithCorrectIdAndDto() {
            PublisherRequestRejectDTO dto = buildRejectDTO();
            when(publisherRequestService.rejectRequest(anyLong(), any(PublisherRequestRejectDTO.class)))
                .thenReturn(buildResponseDTO(RequestStatus.REJECTED, dto.getRejectionReason()));

            publisherRequestController.rejectRequest(42L, dto);

            verify(publisherRequestService).rejectRequest(eq(42L), eq(dto));
        }
    }
}
