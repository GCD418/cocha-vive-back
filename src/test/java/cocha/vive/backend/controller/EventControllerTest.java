package cocha.vive.backend.controller;

import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.EventStatus;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventController Tests")
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    // ─── Fixtures ───────────────────────────────────────────────────────────────

    private Event buildEvent() {
        return Event.builder()
            .id(1L)
            .title("Concierto Rock")
            .shortDescription("Un gran concierto")
            .description("Descripción completa del concierto")
            .cost(50)
            .latitude(-17.3895)
            .longitude(-66.1568)
            .shortPlaceDescription("Plaza Principal")
            .peopleCapacity(500)
            .dateStart(LocalDateTime.now().plusDays(1))
            .dateEnd(LocalDateTime.now().plusDays(1).plusHours(3))
            .tags(List.of("música", "rock"))
            .photoLinks(List.of("https://example.com/photo1.jpg"))
            .eventStatus(EventStatus.APPROVED)
            .isFeatured(false)
            .isActive(true)
            .build();
    }

    private EventRequest buildEventRequest() {
        EventRequest dto = new EventRequest();
        dto.setTitle("Concierto Rock");
        dto.setShortDescription("Un gran concierto");
        dto.setDescription("Descripción completa del concierto");
        dto.setCost(50);
        dto.setCategoryId(1L);
        dto.setLatitude(-17.3895);
        dto.setLongitude(-66.1568);
        dto.setShortPlaceDescription("Plaza Principal");
        dto.setPeopleCapacity(500);
        dto.setDateStart(LocalDateTime.now().plusDays(1));
        dto.setDateEnd(LocalDateTime.now().plusDays(1).plusHours(3));
        dto.setTags(List.of("música", "rock"));
        dto.setPhotoLinks(List.of("https://example.com/photo1.jpg"));
        return dto;
    }

    private List<MultipartFile> buildImages() {
        return List.of(
            new MockMultipartFile("images", "photo1.jpg", "image/jpeg", new byte[]{1, 2, 3})
        );
    }

    // ─── GET /api/events ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/events")
    class GetAllEvents {

        @Test
        @DisplayName("retorna lista de eventos")
        void shouldReturnListOfEvents() {
            List<Event> events = List.of(buildEvent(), buildEvent());
            when(eventService.getAllPublic()).thenReturn(events);

            List<Event> response = eventController.getAllEvents();

            assertThat(response).hasSize(2);
            verify(eventService, times(1)).getAllPublic();
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay eventos")
        void shouldReturnEmptyListWhenNoEvents() {
            when(eventService.getAllPublic()).thenReturn(List.of());

            List<Event> response = eventController.getAllEvents();

            assertThat(response).isEmpty();
            verify(eventService, times(1)).getAllPublic();
        }
    }

    // ─── POST /api/events ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/events")
    class CreateEvent {

        @Test
        @DisplayName("201 – crea evento correctamente")
        void shouldReturn201WhenEventCreated() {
            Event event = buildEvent();
            EventRequest dto = buildEventRequest();
            List<MultipartFile> images = buildImages();
            when(eventService.create(dto, images)).thenReturn(event);

            ResponseEntity<Event> response = eventController.createEvent(dto, images);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getTitle()).isEqualTo("Concierto Rock");
            verify(eventService, times(1)).create(dto, images);
        }

        @Test
        @DisplayName("delega correctamente al servicio con dto e imágenes")
        void shouldDelegateToServiceWithDtoAndImages() {
            EventRequest dto = buildEventRequest();
            List<MultipartFile> images = buildImages();
            when(eventService.create(any(EventRequest.class), anyList())).thenReturn(buildEvent());

            eventController.createEvent(dto, images);

            verify(eventService).create(eq(dto), eq(images));
        }
    }

    // ─── GET /api/events/{id} ────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/events/{id}")
    class GetEventById {

        @Test
        @DisplayName("200 – retorna evento por id")
        void shouldReturn200WithEventById() {
            Event event = buildEvent();
            when(eventService.findById(1L)).thenReturn(event);

            ResponseEntity<Event> response = eventController.getEventById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getTitle()).isEqualTo("Concierto Rock");
            verify(eventService, times(1)).findById(1L);
        }

        @Test
        @DisplayName("delega al servicio con el id correcto")
        void shouldDelegateToServiceWithCorrectId() {
            when(eventService.findById(anyLong())).thenReturn(buildEvent());

            eventController.getEventById(99L);

            verify(eventService).findById(99L);
        }
    }

    // ─── GET /api/events/upcoming ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/events/upcoming")
    class GetUpcomingEvents {

        @Test
        @DisplayName("retorna lista de eventos próximos")
        void shouldReturnUpcomingEvents() {
            List<Event> events = List.of(buildEvent());
            when(eventService.getUpcoming()).thenReturn(events);

            List<Event> response = eventController.getUpcomingEvents();

            assertThat(response).hasSize(1);
            verify(eventService, times(1)).getUpcoming();
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay eventos próximos")
        void shouldReturnEmptyListWhenNoUpcomingEvents() {
            when(eventService.getUpcoming()).thenReturn(List.of());

            List<Event> response = eventController.getUpcomingEvents();

            assertThat(response).isEmpty();
        }
    }

    // ─── GET /api/events/featured ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/events/featured")
    class GetFeaturedEvents {

        @Test
        @DisplayName("retorna lista de eventos destacados")
        void shouldReturnFeaturedEvents() {
            List<Event> events = List.of(buildEvent());
            when(eventService.getFeatured()).thenReturn(events);

            List<Event> response = eventController.getFeaturedEvents();

            assertThat(response).hasSize(1);
            verify(eventService, times(1)).getFeatured();
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay eventos destacados")
        void shouldReturnEmptyListWhenNoFeaturedEvents() {
            when(eventService.getFeatured()).thenReturn(List.of());

            List<Event> response = eventController.getFeaturedEvents();

            assertThat(response).isEmpty();
        }
    }

    // ─── PATCH /api/events/{id}/cancel ───────────────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/events/{id}/cancel")
    class CancelEvent {

        @Test
        @DisplayName("204 – cancela evento correctamente")
        void shouldReturn204WhenEventCancelled() {
            doNothing().when(eventService).cancelEvent(1L);

            ResponseEntity<Void> response = eventController.cancelEvent(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(eventService, times(1)).cancelEvent(1L);
        }

        @Test
        @DisplayName("delega al servicio con el id correcto")
        void shouldDelegateToServiceWithCorrectId() {
            doNothing().when(eventService).cancelEvent(anyLong());

            eventController.cancelEvent(42L);

            verify(eventService).cancelEvent(42L);
        }
    }

    // ─── GET /api/events/category/{categoryId} ───────────────────────────────────

    @Nested
    @DisplayName("GET /api/events/category/{categoryId}")
    class GetEventsByCategory {

        @Test
        @DisplayName("retorna eventos filtrados por categoría")
        void shouldReturnEventsByCategory() {
            List<Event> events = List.of(buildEvent());
            when(eventService.getEventsByCategoryId(1L)).thenReturn(events);

            List<Event> response = eventController.getEventsByCategory(1L);

            assertThat(response).hasSize(1);
            verify(eventService, times(1)).getEventsByCategoryId(1L);
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay eventos en esa categoría")
        void shouldReturnEmptyListWhenNoCategoryEvents() {
            when(eventService.getEventsByCategoryId(anyLong())).thenReturn(List.of());

            List<Event> response = eventController.getEventsByCategory(99L);

            assertThat(response).isEmpty();
        }
    }

    // ─── PUT /api/events/{id} ────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/events/{id}")
    class UpdateEvent {

        @Test
        @DisplayName("200 – actualiza evento correctamente")
        void shouldReturn200WhenEventUpdated() {
            Event updated = buildEvent();
            EventRequest dto = buildEventRequest();
            List<MultipartFile> images = buildImages();
            when(eventService.update(1L, dto, images)).thenReturn(updated);

            ResponseEntity<Event> response = eventController.updateEvent(1L, dto, images);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            verify(eventService, times(1)).update(1L, dto, images);
        }

        @Test
        @DisplayName("200 – actualiza evento sin imágenes (images null)")
        void shouldReturn200WhenUpdatedWithoutImages() {
            Event updated = buildEvent();
            EventRequest dto = buildEventRequest();
            when(eventService.update(anyLong(), any(EventRequest.class), isNull())).thenReturn(updated);

            ResponseEntity<Event> response = eventController.updateEvent(1L, dto, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(eventService).update(eq(1L), eq(dto), isNull());
        }

        @Test
        @DisplayName("delega al servicio con id, dto e imágenes correctos")
        void shouldDelegateToServiceWithCorrectParams() {
            EventRequest dto = buildEventRequest();
            List<MultipartFile> images = buildImages();
            when(eventService.update(anyLong(), any(EventRequest.class), anyList()))
                .thenReturn(buildEvent());

            eventController.updateEvent(5L, dto, images);

            verify(eventService).update(eq(5L), eq(dto), eq(images));
        }
    }
}
