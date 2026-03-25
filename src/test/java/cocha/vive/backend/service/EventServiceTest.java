package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.*;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.repository.CategoryRepository;
import cocha.vive.backend.repository.EventRepository;
import cocha.vive.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldReturnAllEvents() {
        when(eventRepository.findAll()).thenReturn(List.of(new Event(), new Event()));

        List<Event> result = eventService.getAll();

        assertEquals(2, result.size());
        verify(eventRepository).findAll();
    }

    @Test
    void shouldCreateEventSuccessfully() {
        EventRequest dto = new EventRequest();
        dto.setTitle("Test Event");
        dto.setCategoryId(1L);

        User user = new User();
        user.setId(10L);

        Category category = new Category();
        category.setId(1L);

        when(auditService.getActualUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(cloudinaryService.uploadImages(any())).thenReturn(List.of("img1.jpg"));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event result = eventService.create(dto, List.of());

        assertNotNull(result);
        assertEquals("Test Event", result.getTitle());
        assertTrue(result.getIsActive());
        assertEquals(EventStatus.APPROVED, result.getEventStatus());

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        EventRequest dto = new EventRequest();
        dto.setCategoryId(1L);

        when(auditService.getActualUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> eventService.create(dto, List.of()));
    }

    @Test
    void shouldThrowWhenCategoryNotFound() {
        EventRequest dto = new EventRequest();
        dto.setCategoryId(1L);

        when(auditService.getActualUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> eventService.create(dto, List.of()));
    }

    @Test
    void shouldReturnEventById() {
        Event event = new Event();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Event result = eventService.findById(1L);

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenEventNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> eventService.findById(1L));
    }

    @Test
    void shouldUpdateStatus() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(auditService.getActualUserId()).thenReturn(10L);
        when(eventRepository.updateStatus(1L, EventStatus.APPROVED, 10L))
            .thenReturn(1);

        eventService.updateStatus(1L, EventStatus.APPROVED);

        verify(eventRepository).updateStatus(1L, EventStatus.APPROVED, 10L);
    }

    @Test
    void shouldThrowWhenEventNotExists() {
        when(eventRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
            () -> eventService.updateStatus(1L, EventStatus.APPROVED));
    }

    @Test
    void shouldThrowWhenUpdateFails() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(auditService.getActualUserId()).thenReturn(10L);
        when(eventRepository.updateStatus(1L, EventStatus.APPROVED, 10L))
            .thenReturn(0);

        assertThrows(EntityNotFoundException.class,
            () -> eventService.updateStatus(1L, EventStatus.APPROVED));
    }

    @Test
    void shouldCancelEvent() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(auditService.getActualUserId()).thenReturn(10L);
        when(eventRepository.updateStatus(1L, EventStatus.CANCELLED, 10L))
            .thenReturn(1);

        eventService.cancelEvent(1L);

        verify(eventRepository).updateStatus(1L, EventStatus.CANCELLED, 10L);
    }

    @Test
    void shouldUpdateEvent() {
        User currentUser = new User();
        currentUser.setId(10L);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Event event = new Event();
        event.setId(1L);
        event.setOrganizedByUser(currentUser);

        EventRequest dto = new EventRequest();
        dto.setTitle("Updated Title");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Event result = eventService.update(1L, dto, List.of());

        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void shouldThrowWhenUserNotOwner() {
        User owner = new User();
        owner.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(otherUser);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Event event = new Event();
        event.setOrganizedByUser(owner);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(AccessDeniedException.class,
            () -> eventService.update(1L, new EventRequest(), List.of()));
    }
}
