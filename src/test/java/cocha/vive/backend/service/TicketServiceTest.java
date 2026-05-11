package cocha.vive.backend.service;

import cocha.vive.backend.exception.InvalidStateTransitionException;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.Ticket;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.TicketResponseDTO;
import cocha.vive.backend.model.mapper.TicketMapper;
import cocha.vive.backend.repository.EventRepository;
import cocha.vive.backend.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService tests")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private QrCodeService qrCodeService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void getMyTickets_shouldReturnTicketDtos() {
        User buyer = new User();
        buyer.setId(33L);

        Ticket ticket = Ticket.builder().id(UUID.randomUUID()).build();
        TicketResponseDTO dto = new TicketResponseDTO(
            ticket.getId(), 1, 100L, 100L, false, false, 10L,
            "Evento", "Music", LocalDateTime.now(), LocalDateTime.now().plusHours(2), 33L, null
        );

        when(userService.getActualUser()).thenReturn(buyer);
        when(ticketRepository.findAllByBuyerUserIdIdOrderByCreatedAtDesc(33L))
            .thenReturn(List.of(ticket));
        when(ticketMapper.toResponseDtoList(List.of(ticket))).thenReturn(List.of(dto));

        List<TicketResponseDTO> result = ticketService.getMyTickets();

        assertThat(result).hasSize(1);
        verify(ticketRepository).findAllByBuyerUserIdIdOrderByCreatedAtDesc(33L);
        verify(ticketMapper).toResponseDtoList(List.of(ticket));
    }

    @Test
    void markUsed_shouldMarkTicketAsUsed() {
        User organizer = new User();
        organizer.setId(10L);

        Event event = new Event();
        event.setOrganizedByUser(organizer);
        event.setDateEnd(LocalDateTime.now().plusDays(1));

        Ticket ticket = new Ticket();
        UUID ticketId = UUID.randomUUID();
        ticket.setId(ticketId);
        ticket.setEvent(event);
        ticket.setUsed(false);

        when(userService.getActualUser()).thenReturn(organizer);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        ticketService.markUsed(ticketId);

        assertThat(ticket.getUsed()).isTrue();
        verify(ticketRepository).save(ticket);
    }

    @Test
    void markUsed_shouldThrowWhenTicketNotFound() {
        User organizer = new User();
        organizer.setId(10L);

        when(userService.getActualUser()).thenReturn(organizer);
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.markUsed(ticketId));
    }

    @Test
    void markUsed_shouldThrowWhenUserNotOrganizer() {
        User organizer = new User();
        organizer.setId(10L);

        User otherUser = new User();
        otherUser.setId(11L);

        Event event = new Event();
        event.setOrganizedByUser(organizer);

        Ticket ticket = new Ticket();
        UUID ticketId = UUID.randomUUID();
        ticket.setId(ticketId);
        ticket.setEvent(event);
        ticket.setUsed(false);

        when(userService.getActualUser()).thenReturn(otherUser);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(AccessDeniedException.class, () -> ticketService.markUsed(ticketId));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void markUsed_shouldThrowWhenTicketAlreadyUsed() {
        User organizer = new User();
        organizer.setId(10L);

        Event event = new Event();
        event.setOrganizedByUser(organizer);

        Ticket ticket = new Ticket();
        UUID ticketId = UUID.randomUUID();
        ticket.setId(ticketId);
        ticket.setEvent(event);
        ticket.setUsed(true);

        when(userService.getActualUser()).thenReturn(organizer);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStateTransitionException.class, () -> ticketService.markUsed(ticketId));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void markUsed_shouldThrowWhenTicketExpired() {
        User organizer = new User();
        organizer.setId(10L);

        Event event = new Event();
        event.setOrganizedByUser(organizer);

        Ticket ticket = mock(Ticket.class);
        when(ticket.getEvent()).thenReturn(event);
        when(ticket.getUsed()).thenReturn(false);
        when(ticket.isExpired()).thenReturn(true);

        when(userService.getActualUser()).thenReturn(organizer);
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStateTransitionException.class, () -> ticketService.markUsed(ticketId));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void createTicket_shouldPersistAndSendEmail() {
        User buyer = new User();
        buyer.setId(33L);
        buyer.setEmail("buyer@example.com");

        Event event = new Event();
        event.setId(99L);
        event.setTitle("Evento");
        event.setCost(10000);

        UUID ticketId = UUID.randomUUID();
        Ticket saved = Ticket.builder()
            .id(ticketId)
            .buyerUserId(buyer)
            .event(event)
            .quantity(2)
            .unitPrice(10000L)
            .used(false)
            .build();

        TicketResponseDTO dto = new TicketResponseDTO(
            ticketId, 2, 10000L, 20000L, false, false, 99L,
            "Evento", "Music", LocalDateTime.now(), LocalDateTime.now().plusHours(2), 33L, null
        );

        when(userService.getActualUser()).thenReturn(buyer);
        when(eventRepository.findById(99L)).thenReturn(Optional.of(event));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(saved);
        byte[] qr = new byte[] { 1, 2, 3 };
        when(qrCodeService.generatePng("TICKET:" + ticketId, 240, 240)).thenReturn(qr);
        when(ticketMapper.toResponseDto(saved)).thenReturn(dto);

        TicketResponseDTO result = ticketService.createTicket(99L, 2);

        assertThat(result.id()).isEqualTo(ticketId);
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());
        assertThat(ticketCaptor.getValue().getUnitPrice()).isEqualTo(10000L);
        verify(emailService).sendTicketPurchasedEmail(buyer, saved, qr);
    }
}
