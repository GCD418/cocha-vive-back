package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.*;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.model.mapper.EventMapper;
import cocha.vive.backend.repository.CategoryRepository;
import cocha.vive.backend.repository.EventRepository;
import cocha.vive.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock
    private EventMapper eventMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventService eventService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnAllEvents() {
        when(eventRepository.findAllPublic()).thenReturn(List.of(new Event(), new Event()));

        List<Event> result = eventService.getAllPublic();

        assertEquals(2, result.size());
        verify(eventRepository).findAllPublic();
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

        Event mappedEvent = new Event();
        mappedEvent.setTitle("Test Event");
        mappedEvent.setIsActive(true);
        mappedEvent.setEventStatus(EventStatus.APPROVED);
        mappedEvent.setOrganizedByUser(user);

        User admin = new User();
        admin.setId(20L);
        admin.setEmail("admin@mail.com");

        when(auditService.getActualUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(cloudinaryService.uploadImages(any())).thenReturn(List.of("img1.jpg"));
        when(eventMapper.toEntity(eq(dto), eq(category), eq(user), anyList())).thenReturn(mappedEvent);
        when(eventRepository.save(mappedEvent)).thenReturn(mappedEvent);
        when(userService.getAllAdmins()).thenReturn(List.of(admin));

        Event result = eventService.create(dto, List.of());

        assertNotNull(result);
        assertEquals("Test Event", result.getTitle());
        assertTrue(result.getIsActive());
        assertEquals(EventStatus.APPROVED, result.getEventStatus());

        verify(eventMapper).toEntity(eq(dto), eq(category), eq(user), anyList());
        verify(eventRepository).save(mappedEvent);
        verify(emailService).sendNewEventWantsToBePublishedEmail(admin, mappedEvent);
    }

    @Test
    void shouldSendNotificationToEachAdminWhenCreatingEvent() {
        EventRequest dto = new EventRequest();
        dto.setTitle("Admin notification event");
        dto.setCategoryId(11L);

        User creator = new User();
        creator.setId(44L);

        Category category = new Category();
        category.setId(11L);

        Event mappedEvent = new Event();
        mappedEvent.setTitle("Admin notification event");
        mappedEvent.setOrganizedByUser(creator);

        User admin1 = new User();
        admin1.setId(1L);
        User admin2 = new User();
        admin2.setId(2L);

        when(auditService.getActualUserId()).thenReturn(44L);
        when(userRepository.findById(44L)).thenReturn(Optional.of(creator));
        when(categoryRepository.findById(11L)).thenReturn(Optional.of(category));
        when(cloudinaryService.uploadImages(any())).thenReturn(List.of());
        when(eventMapper.toEntity(eq(dto), eq(category), eq(creator), anyList())).thenReturn(mappedEvent);
        when(eventRepository.save(mappedEvent)).thenReturn(mappedEvent);
        when(userService.getAllAdmins()).thenReturn(List.of(admin1, admin2));

        eventService.create(dto, List.of());

        verify(emailService).sendNewEventWantsToBePublishedEmail(admin1, mappedEvent);
        verify(emailService).sendNewEventWantsToBePublishedEmail(admin2, mappedEvent);
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

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(currentUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        Event event = new Event();
        event.setId(1L);
        event.setOrganizedByUser(currentUser);

        EventRequest dto = new EventRequest();
        dto.setTitle("Updated Title");

        doAnswer(invocation -> {
            EventRequest requestArg = invocation.getArgument(0);
            Event eventArg = invocation.getArgument(1);
            eventArg.setTitle(requestArg.getTitle());
            return null;
        }).when(eventMapper).updateEventFromRequest(any(EventRequest.class), any(Event.class));

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

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(otherUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        Event event = new Event();
        event.setOrganizedByUser(owner);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(AccessDeniedException.class,
            () -> eventService.update(1L, new EventRequest(), List.of()));
    }
    @Test
    void shouldReturnUpcomingEvents() {
        when(eventRepository.findActiveUpcoming())
            .thenReturn(List.of(new Event(), new Event()));

        List<Event> result = eventService.getUpcoming();

        assertEquals(2, result.size());
        verify(eventRepository).findActiveUpcoming();
    }
    @Test
    void shouldReturnFeaturedEvents() {
        when(eventRepository.findActiveFeatured())
            .thenReturn(List.of(new Event()));

        List<Event> result = eventService.getFeatured();

        assertEquals(1, result.size());
        verify(eventRepository).findActiveFeatured();
    }
    @Test
    void shouldReturnEventsByCategoryId() {
        when(eventRepository.findByCategoryId(1L))
            .thenReturn(List.of(new Event()));

        List<Event> result = eventService.getEventsByCategoryId(1L);

        assertEquals(1, result.size());
        verify(eventRepository).findByCategoryId(1L);
    }
    @Test
    void shouldThrowWhenUpdatingWithInvalidCategory() {
        User user = new User();
        user.setId(1L);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        Event event = new Event();
        event.setOrganizedByUser(user);

        EventRequest dto = new EventRequest();
        dto.setCategoryId(99L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> eventService.update(1L, dto, List.of()));
    }
    @Test
    void shouldMergeExistingAndNewImages() {
        User user = new User();
        user.setId(1L);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        Event event = new Event();
        event.setOrganizedByUser(user);
        event.setPhotoLinks(List.of("old.jpg"));

        EventRequest dto = new EventRequest();
        dto.setPhotoLinks(List.of("existing.jpg"));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(cloudinaryService.uploadImages(any()))
            .thenReturn(List.of("new.jpg"));
        when(eventRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Event result = eventService.update(1L, dto,
            List.of(mock(org.springframework.web.multipart.MultipartFile.class)));

        assertEquals(2, result.getPhotoLinks().size());
        verify(cloudinaryService).uploadImages(any());
    }
    @Test
    void shouldSetModifiedByUserId() {
        User user = new User();
        user.setId(99L);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        Event event = new Event();
        event.setOrganizedByUser(user);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Event result = eventService.update(1L, new EventRequest(), List.of());

        assertEquals(99L, result.getModifiedByUserId());
    }
}
