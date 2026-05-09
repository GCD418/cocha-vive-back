package cocha.vive.backend.service;

import cocha.vive.backend.exception.InvalidStateTransitionException;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Ticket;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.TicketResponseDTO;
import cocha.vive.backend.model.mapper.TicketMapper;
import cocha.vive.backend.repository.EventRepository;
import cocha.vive.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final TicketMapper ticketMapper;
    private final EmailService emailService;
    private final QrCodeService qrCodeService;

    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getMyTickets() {
        User actualUser = userService.getActualUser();
        log.debug("Retrieving tickets for buyer id: {}", actualUser.getId());
        List<Ticket> tickets = ticketRepository
            .findAllByBuyerUserIdIdOrderByCreatedAtDesc(actualUser.getId());
        log.debug("Retrieved {} ticket(s) for buyer id: {}", tickets.size(), actualUser.getId());
        return ticketMapper.toResponseDtoList(tickets);
    }

    @Transactional
    public void markUsed(Long ticketId) {
        User actualUser = userService.getActualUser();
        log.info("Marking ticket id: {} as used by organizer id: {}", ticketId, actualUser.getId());

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> {
                log.warn("Ticket not found with id: {}", ticketId);
                return new ResourceNotFoundException("Ticket not found");
            });

        Long organizerId = ticket.getEvent().getOrganizedByUser().getId();
        if (!organizerId.equals(actualUser.getId())) {
            log.warn("User id: {} attempted to mark ticket id: {} without permission", actualUser.getId(), ticketId);
            throw new AccessDeniedException("You are not allowed to mark this ticket as used");
        }

        if (Boolean.TRUE.equals(ticket.getUsed())) {
            log.warn("Ticket id: {} already used", ticketId);
            throw new InvalidStateTransitionException("Ticket already used");
        }

        if (Boolean.TRUE.equals(ticket.isExpired())) {
            log.warn("Ticket id: {} is expired", ticketId);
            throw new InvalidStateTransitionException("Ticket is expired");
        }

        ticket.setUsed(true);
        ticketRepository.save(ticket);
        log.info("Ticket id: {} marked as used", ticketId);
    }

    @Transactional
    public TicketResponseDTO createTicket(Long eventId, Integer quantity, Long unitPrice) {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unitPrice must be non-negative");
        }

        User buyer = userService.getActualUser();
        log.info("Creating ticket for buyer id: {} for event id: {}", buyer.getId(), eventId);

        var event = eventRepository.findById(eventId)
            .orElseThrow(() -> {
                log.warn("Event not found with id: {} for ticket creation", eventId);
                return new ResourceNotFoundException("Event not found");
            });

        Ticket ticket = Ticket.builder()
            .event(event)
            .buyerUserId(buyer)
            .quantity(quantity)
            .unitPrice(unitPrice)
            .used(false)
            .build();

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket created with id: {} for buyer id: {}", saved.getId(), buyer.getId());

        String qrPayload = "TICKET:" + saved.getId();
        String qrBase64 = qrCodeService.generatePngBase64(qrPayload, 240, 240);
        emailService.sendTicketPurchasedEmail(buyer, saved, qrBase64);

        return ticketMapper.toResponseDto(saved);
    }
}
