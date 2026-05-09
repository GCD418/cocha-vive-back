package cocha.vive.backend.service;

import cocha.vive.backend.exception.InvalidStateTransitionException;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.Ticket;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.TicketResponseDTO;
import cocha.vive.backend.model.mapper.TicketMapper;
import cocha.vive.backend.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService tests")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserService userService;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void getMyTickets_shouldReturnTicketDtos() {
        User buyer = new User();
        buyer.setId(33L);

        Ticket ticket = Ticket.builder().id(1L).build();
        TicketResponseDTO dto = new TicketResponseDTO(1L, 1, 100L, 100L, false, 10L, 33L, null);

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

        Ticket ticket = new Ticket();
        ticket.setId(5L);
        ticket.setEvent(event);
        ticket.setUsed(false);

        when(userService.getActualUser()).thenReturn(organizer);
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        ticketService.markUsed(5L);

        assertThat(ticket.getUsed()).isTrue();
        verify(ticketRepository).save(ticket);
    }

    @Test
    void markUsed_shouldThrowWhenTicketNotFound() {
        User organizer = new User();
        organizer.setId(10L);

        when(userService.getActualUser()).thenReturn(organizer);
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.markUsed(99L));
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
        ticket.setId(5L);
        ticket.setEvent(event);
        ticket.setUsed(false);

        when(userService.getActualUser()).thenReturn(otherUser);
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        assertThrows(AccessDeniedException.class, () -> ticketService.markUsed(5L));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void markUsed_shouldThrowWhenTicketAlreadyUsed() {
        User organizer = new User();
        organizer.setId(10L);

        Event event = new Event();
        event.setOrganizedByUser(organizer);

        Ticket ticket = new Ticket();
        ticket.setId(5L);
        ticket.setEvent(event);
        ticket.setUsed(true);

        when(userService.getActualUser()).thenReturn(organizer);
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStateTransitionException.class, () -> ticketService.markUsed(5L));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void markUsed_shouldThrowWhenTicketExpired() {
        User organizer = new User();
        organizer.setId(10L);

        Event event = new Event();
        event.setOrganizedByUser(organizer);

        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(5L);
        when(ticket.getEvent()).thenReturn(event);
        when(ticket.getUsed()).thenReturn(false);
        when(ticket.isExpired()).thenReturn(true);

        when(userService.getActualUser()).thenReturn(organizer);
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStateTransitionException.class, () -> ticketService.markUsed(5L));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}
