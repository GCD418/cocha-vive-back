package cocha.vive.backend.service;

import cocha.vive.backend.exception.InvalidStateTransitionException;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.EventPromotion;
import cocha.vive.backend.model.EventStatus;
import cocha.vive.backend.model.PromotionPlan;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.PromotionResponseDTO;
import cocha.vive.backend.model.mapper.EventPromotionMapper;
import cocha.vive.backend.repository.EventPromotionRepository;
import cocha.vive.backend.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromotionService tests")
class PromotionServiceTest {

    @Mock
    private EventPromotionRepository eventPromotionRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventPromotionMapper eventPromotionMapper;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private QrCodeService qrCodeService;

    @InjectMocks
    private PromotionEventService promotionService;

    // ─── Fixtures ───────────────────────────────────────────────

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("publisher@example.com");
        return user;
    }

    private Event buildApprovedEvent(Long eventId, User owner) {
        Event event = new Event();
        event.setId(eventId);
        event.setTitle("Concierto Rock");
        event.setOrganizedByUser(owner);
        event.setEventStatus(EventStatus.APPROVED);
        event.setIsFeatured(false);
        return event;
    }

    // ─── Camino feliz ────────────────────────────────────────────

    @Nested
    @DisplayName("purchasePromotion - success")
    class Success {

        @Test
        @DisplayName("persiste promoción, genera QR, envía correo y notificación")
        void shouldPersistPromotionAndNotify() {
            User owner = buildUser(10L);
            Event event = buildApprovedEvent(99L, owner);

            when(userService.getActualUser()).thenReturn(owner);
            when(eventRepository.findById(99L)).thenReturn(Optional.of(event));
            when(eventPromotionRepository.findTopByEventIdOrderByEndAtDesc(99L))
                .thenReturn(Optional.empty());
            when(eventPromotionRepository.save(any(EventPromotion.class)))
                .thenAnswer(i -> {
                    EventPromotion p = i.getArgument(0);
                    p.setId(UUID.randomUUID());
                    return p;
                });
            byte[] qr = new byte[]{1, 2, 3};
            when(qrCodeService.generatePng(anyString(), eq(240), eq(240))).thenReturn(qr);
            when(eventPromotionMapper.toResponseDto(any(EventPromotion.class)))
                .thenReturn(mock(PromotionResponseDTO.class));

            promotionService.purchasePromotion(99L, PromotionPlan.ONE_WEEK);

            ArgumentCaptor<EventPromotion> captor = ArgumentCaptor.forClass(EventPromotion.class);
            verify(eventPromotionRepository).save(captor.capture());
            assertThat(captor.getValue().getAmount()).isEqualTo(90L);
            assertThat(captor.getValue().getPlan()).isEqualTo(PromotionPlan.ONE_WEEK);

            verify(emailService).sendEventPromotedEmail(eq(owner), any(EventPromotion.class), eq(qr));
            verify(notificationService).create(eq(owner), anyString(), anyString());
        }

        @Test
        @DisplayName("primera promoción: la ventana empieza ahora")
        void shouldStartWindowNowWhenNoPreviousPromotion() {
            User owner = buildUser(10L);
            Event event = buildApprovedEvent(99L, owner);

            when(userService.getActualUser()).thenReturn(owner);
            when(eventRepository.findById(99L)).thenReturn(Optional.of(event));
            when(eventPromotionRepository.findTopByEventIdOrderByEndAtDesc(99L))
                .thenReturn(Optional.empty());
            when(eventPromotionRepository.save(any(EventPromotion.class)))
                .thenAnswer(i -> i.getArgument(0));
            when(qrCodeService.generatePng(anyString(), anyInt(), anyInt())).thenReturn(new byte[]{1});
            when(eventPromotionMapper.toResponseDto(any())).thenReturn(mock(PromotionResponseDTO.class));

            promotionService.purchasePromotion(99L, PromotionPlan.ONE_DAY);

            ArgumentCaptor<EventPromotion> captor = ArgumentCaptor.forClass(EventPromotion.class);
            verify(eventPromotionRepository).save(captor.capture());
            EventPromotion saved = captor.getValue();
            assertThat(saved.getStartAt()).isCloseTo(LocalDateTime.now(), within(5, java.time.temporal.ChronoUnit.SECONDS));
            assertThat(saved.getEndAt()).isEqualTo(saved.getStartAt().plusDays(1));
        }

        @Test
        @DisplayName("suma de ventanas: la nueva empieza donde termina la activa")
        void shouldChainWindowWhenActivePromotionExists() {
            User owner = buildUser(10L);
            Event event = buildApprovedEvent(99L, owner);
            LocalDateTime futureEnd = LocalDateTime.now().plusDays(3);

            EventPromotion existing = EventPromotion.builder()
                .endAt(futureEnd)
                .build();

            when(userService.getActualUser()).thenReturn(owner);
            when(eventRepository.findById(99L)).thenReturn(Optional.of(event));
            when(eventPromotionRepository.findTopByEventIdOrderByEndAtDesc(99L))
                .thenReturn(Optional.of(existing));
            when(eventPromotionRepository.save(any(EventPromotion.class)))
                .thenAnswer(i -> i.getArgument(0));
            when(qrCodeService.generatePng(anyString(), anyInt(), anyInt())).thenReturn(new byte[]{1});
            when(eventPromotionMapper.toResponseDto(any())).thenReturn(mock(PromotionResponseDTO.class));

            promotionService.purchasePromotion(99L, PromotionPlan.ONE_WEEK);

            ArgumentCaptor<EventPromotion> captor = ArgumentCaptor.forClass(EventPromotion.class);
            verify(eventPromotionRepository).save(captor.capture());
            EventPromotion saved = captor.getValue();
            assertThat(saved.getStartAt()).isEqualTo(futureEnd);
            assertThat(saved.getEndAt()).isEqualTo(futureEnd.plusDays(7));
        }
    }

    // ─── Autorización / validación ───────────────────────────────

    @Nested
    @DisplayName("purchasePromotion - rejected")
    class Rejected {

        @Test
        @DisplayName("404 cuando el evento no existe")
        void shouldThrowWhenEventNotFound() {
            when(userService.getActualUser()).thenReturn(buildUser(10L));
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                () -> promotionService.purchasePromotion(99L, PromotionPlan.ONE_DAY));

            verify(eventPromotionRepository, never()).save(any());
        }

        @Test
        @DisplayName("403 cuando el usuario no es dueño del evento")
        void shouldThrowWhenUserNotOwner() {
            User owner = buildUser(10L);
            User other = buildUser(11L);
            Event event = buildApprovedEvent(99L, owner);

            when(userService.getActualUser()).thenReturn(other);
            when(eventRepository.findById(99L)).thenReturn(Optional.of(event));

            assertThrows(AccessDeniedException.class,
                () -> promotionService.purchasePromotion(99L, PromotionPlan.ONE_DAY));

            verify(eventPromotionRepository, never()).save(any());
        }

        @Test
        @DisplayName("409 cuando el evento no está APPROVED")
        void shouldThrowWhenEventNotApproved() {
            User owner = buildUser(10L);
            Event event = buildApprovedEvent(99L, owner);
            event.setEventStatus(EventStatus.PENDING);

            when(userService.getActualUser()).thenReturn(owner);
            when(eventRepository.findById(99L)).thenReturn(Optional.of(event));

            assertThrows(InvalidStateTransitionException.class,
                () -> promotionService.purchasePromotion(99L, PromotionPlan.ONE_DAY));

            verify(eventPromotionRepository, never()).save(any());
        }
    }
}
