package cocha.vive.backend.repository;

import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.EventStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventRepository Tests")
class EventRepositoryTest {

    @Mock
    private EventRepository eventRepository;

    // ─── Fixtures ─────────────────────────────────────────────

    private Event buildEvent() {
        return Event.builder()
            .id(1L)
            .title("Concert")
            .shortDescription("Live music")
            .description("Full concert description")
            .cost(50)
            .latitude(-17.38)
            .longitude(-66.16)
            .peopleCapacity(100)
            .dateStart(LocalDateTime.now().plusDays(1))
            .dateEnd(LocalDateTime.now().plusDays(1).plusHours(2))
            .eventStatus(EventStatus.APPROVED)
            .isFeatured(false)
            .isActive(true)
            .build();
    }

    // ─── findActiveUpcoming ───────────────────────────────────

    @Nested
    @DisplayName("findActiveUpcoming")
    class FindActiveUpcoming {

        @Test
        @DisplayName("retorna lista de eventos activos próximos")
        void shouldReturnActiveUpcomingEvents() {
            List<Event> events = List.of(buildEvent(), buildEvent());
            when(eventRepository.findActiveUpcoming()).thenReturn(events);

            List<Event> result = eventRepository.findActiveUpcoming();

            assertThat(result).hasSize(2);
            verify(eventRepository, times(1)).findActiveUpcoming();
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay eventos")
        void shouldReturnEmptyList() {
            when(eventRepository.findActiveUpcoming()).thenReturn(List.of());

            List<Event> result = eventRepository.findActiveUpcoming();

            assertThat(result).isEmpty();
        }
    }

    // ─── findByIsActiveTrueAndIsFeaturedTrue ───────────────────

    @Nested
    @DisplayName("findFeatured")
    class FindFeatured {

        @Test
        @DisplayName("retorna eventos destacados activos")
        void shouldReturnFeaturedEvents() {
            Event featured = buildEvent();
            featured.setIsFeatured(true);

            when(eventRepository.findByIsActiveTrueAndIsFeaturedTrue())
                .thenReturn(List.of(featured));

            List<Event> result = eventRepository.findByIsActiveTrueAndIsFeaturedTrue();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsFeatured()).isTrue();
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay destacados")
        void shouldReturnEmptyWhenNoFeatured() {
            when(eventRepository.findByIsActiveTrueAndIsFeaturedTrue())
                .thenReturn(List.of());

            List<Event> result = eventRepository.findByIsActiveTrueAndIsFeaturedTrue();

            assertThat(result).isEmpty();
        }
    }

    // ─── updateStatus ─────────────────────────────────────────

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("actualiza estado correctamente")
        void shouldUpdateStatus() {
            when(eventRepository.updateStatus(1L, EventStatus.APPROVED, 99L))
                .thenReturn(1);

            int result = eventRepository.updateStatus(1L, EventStatus.APPROVED, 99L);

            assertThat(result).isEqualTo(1);
            verify(eventRepository, times(1))
                .updateStatus(1L, EventStatus.APPROVED, 99L);
        }

        @Test
        @DisplayName("retorna 0 cuando no se actualiza ningún registro")
        void shouldReturnZeroWhenNoRowsUpdated() {
            when(eventRepository.updateStatus(anyLong(), any(), anyLong()))
                .thenReturn(0);

            int result = eventRepository.updateStatus(99L, EventStatus.REJECTED, 1L);

            assertThat(result).isEqualTo(0);
        }
    }

    // ─── findByCategoryId ─────────────────────────────────────

    @Nested
    @DisplayName("findByCategoryId")
    class FindByCategoryId {

        @Test
        @DisplayName("retorna eventos por categoryId")
        void shouldReturnEventsByCategoryId() {
            List<Event> events = List.of(buildEvent());
            when(eventRepository.findByCategoryId(1L)).thenReturn(events);

            List<Event> result = eventRepository.findByCategoryId(1L);

            assertThat(result).hasSize(1);
            verify(eventRepository).findByCategoryId(1L);
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay eventos")
        void shouldReturnEmptyList() {
            when(eventRepository.findByCategoryId(anyLong())).thenReturn(List.of());

            List<Event> result = eventRepository.findByCategoryId(999L);

            assertThat(result).isEmpty();
        }
    }

    // ─── save ─────────────────────────────────────────────────

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("guarda y retorna evento")
        void shouldSaveEvent() {
            Event event = buildEvent();
            when(eventRepository.save(any(Event.class))).thenReturn(event);

            Event result = eventRepository.save(event);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(eventRepository).save(event);
        }
    }

    // ─── findById ─────────────────────────────────────────────

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna evento cuando existe")
        void shouldReturnEventWhenExists() {
            Event event = buildEvent();
            when(eventRepository.findById(1L)).thenReturn(java.util.Optional.of(event));

            var result = eventRepository.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("retorna vacío cuando no existe")
        void shouldReturnEmpty() {
            when(eventRepository.findById(99L)).thenReturn(java.util.Optional.empty());

            var result = eventRepository.findById(99L);

            assertThat(result).isEmpty();
        }
    }
}
