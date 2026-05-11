package cocha.vive.backend.controller;

import cocha.vive.backend.model.dto.BuyTicketRequestDTO;
import cocha.vive.backend.model.dto.TicketResponseDTO;
import cocha.vive.backend.service.TicketService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketController tests")
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    @Nested
    @DisplayName("GET /api/tickets")
    class GetMyTickets {

        @Test
        void shouldReturnTickets() {
            TicketResponseDTO ticket1 = new TicketResponseDTO(
                UUID.randomUUID(), 2, 100L, 200L, false, false, 10L,
                "Festival", "Music", LocalDateTime.now(), LocalDateTime.now().plusHours(3), 20L, LocalDateTime.now());
            TicketResponseDTO ticket2 = new TicketResponseDTO(
                UUID.randomUUID(), 1, 150L, 150L, false, true, 11L,
                "Expo", "Art", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 21L, LocalDateTime.now());

            when(ticketService.getMyTickets()).thenReturn(List.of(ticket1, ticket2));

            ResponseEntity<List<TicketResponseDTO>> response = ticketController.getMyTickets();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
            verify(ticketService).getMyTickets();
        }
    }

    @Nested
    @DisplayName("POST /api/tickets/buy")
    class BuyTicket {

        @Test
        void shouldCreateTicket() {
            UUID ticketId = UUID.randomUUID();
            TicketResponseDTO responseDTO = new TicketResponseDTO(
                ticketId, 1, 10000L, 10000L, false, false, 10L,
                "Festival", "Music", LocalDateTime.now(), LocalDateTime.now().plusHours(2), 20L, LocalDateTime.now());

            when(ticketService.createTicket(10L, 1)).thenReturn(responseDTO);

            BuyTicketRequestDTO requestDTO = new BuyTicketRequestDTO();
            requestDTO.setEventId(10L);
            requestDTO.setQuantity(1);

            ResponseEntity<TicketResponseDTO> response = ticketController.buyTicket(requestDTO);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(responseDTO);
            verify(ticketService).createTicket(10L, 1);
        }
    }

    @Nested
    @DisplayName("PATCH /api/tickets/{id}/use")
    class MarkTicketAsUsed {

        @Test
        void shouldReturnNoContent() {
            UUID ticketId = UUID.randomUUID();
            doNothing().when(ticketService).markUsed(ticketId);

            ResponseEntity<Void> response = ticketController.markTicketAsUsed(ticketId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(ticketService).markUsed(ticketId);
        }
    }
}
