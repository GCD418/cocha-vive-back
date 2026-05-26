package cocha.vive.backend.service;

import cocha.vive.backend.exception.InvalidStateTransitionException;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Currency;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.EventPromotion;
import cocha.vive.backend.model.EventStatus;
import cocha.vive.backend.model.PromotionPlan;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.PromotionResponseDTO;
import cocha.vive.backend.model.mapper.EventPromotionMapper;
import cocha.vive.backend.repository.EventPromotionRepository;
import cocha.vive.backend.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionEventService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final EventPromotionRepository eventPromotionRepository;
    private final EventRepository eventRepository;
    private final EventPromotionMapper eventPromotionMapper;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final QrCodeService qrCodeService;

    @Transactional
    public PromotionResponseDTO purchasePromotion(Long eventId, PromotionPlan plan) {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(plan, "plan must not be null");

        User buyer = userService.getActualUser();
        log.info("Promotion purchase requested by user id: {} for event id: {} plan: {}",
            buyer.getId(), eventId, plan);

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> {
                log.warn("Event not found with id: {} for promotion purchase", eventId);
                return new ResourceNotFoundException("Event not found");
            });

        if (!event.getOrganizedByUser().getId().equals(buyer.getId())) {
            log.warn("User id: {} attempted to promote event id: {} without ownership",
                buyer.getId(), eventId);
            throw new AccessDeniedException("You are not allowed to promote this event");
        }

        if (event.getEventStatus() != EventStatus.APPROVED) {
            log.warn("Event id: {} is not APPROVED (status: {}); cannot be promoted",
                eventId, event.getEventStatus());
            throw new InvalidStateTransitionException("Only approved events can be promoted");
        }

        LocalDateTime startAt = resolveWindowStart(eventId);
        LocalDateTime endAt = startAt.plus(plan.getDuration());

        EventPromotion promotion = EventPromotion.builder()
            .event(event)
            .purchasedByUser(buyer)
            .plan(plan)
            .amount(plan.getAmount())
            .currency(Currency.BOB)
            .startAt(startAt)
            .endAt(endAt)
            .build();

        EventPromotion saved = eventPromotionRepository.save(promotion);
        log.info("Promotion created with id: {} for event id: {} window: {} -> {}",
            saved.getId(), eventId, startAt, endAt);

        if (Boolean.FALSE.equals(event.getIsFeatured())) {
            event.setIsFeatured(true);
            eventRepository.save(event);
        }

        String qrPayload = "PROMOTION:" + saved.getId();
        byte[] qrPng = qrCodeService.generatePng(qrPayload, 240, 240);

        String eventTitle = event.getTitle();
        String planName = plan.name();
        String formattedStart = DATE_TIME_FORMATTER.format(saved.getStartAt());
        String formattedEnd = DATE_TIME_FORMATTER.format(saved.getEndAt());

        emailService.sendEventPromotedEmail(buyer, eventTitle, planName,
            plan.getAmount(), formattedStart, formattedEnd, qrPng);

        notificationService.create(
            buyer,
            "Evento destacado",
            "Tu evento \"" + event.getTitle() + "\" fue destacado con el plan " + plan.name() + "."
        );

        return eventPromotionMapper.toResponseDto(saved);
    }

    private LocalDateTime resolveWindowStart(Long eventId) {
        LocalDateTime now = LocalDateTime.now();
        return eventPromotionRepository.findTopByEventIdOrderByEndAtDesc(eventId)
            .map(EventPromotion::getEndAt)
            .filter(latestEnd -> latestEnd.isAfter(now))
            .orElse(now);
    }
}
