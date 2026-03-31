package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.EventStatus;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.repository.CategoryRepository;
import cocha.vive.backend.repository.EventRepository;
import cocha.vive.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final AuditService auditService;

    public List<Event> getAll(){
        log.info("Retrieving all events");
        return eventRepository.findAll();
    }

    public Event create(EventRequest dto, List<MultipartFile> images) {
        log.info("Creating event with title: {}", dto.getTitle());
        User user = userRepository.findById(auditService.getActualUserId())
            .orElseThrow(() -> {
                log.warn("User not found with id: {}", auditService.getActualUserId());
                return new ResourceNotFoundException("Usuario no encontrado");
            });
        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> {
                log.warn("Category not found with id: {}", dto.getCategoryId());
                return new ResourceNotFoundException("Categoría no encontrada");
            });
        Event event = Event.builder()
            .title(dto.getTitle())
            .shortDescription(dto.getShortDescription())
            .description(dto.getDescription())
            .cost(dto.getCost())
            .category(category)
            .organizedByUser(user)
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .shortPlaceDescription(dto.getShortPlaceDescription())
            .peopleCapacity(dto.getPeopleCapacity())
            .dateStart(dto.getDateStart())
            .dateEnd(dto.getDateEnd())
            .tags(dto.getTags())
            .photoLinks(cloudinaryService.uploadImages(images))
            .eventStatus(EventStatus.APPROVED)
            .isActive(true)
            .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Event created with id: {}", savedEvent.getId());

        return savedEvent;
    }

    public Event findById(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event not exists with id: " + id));
    }

    public List<Event> getUpcoming() {
        return eventRepository.findActiveUpcoming();
    }

    public List<Event> getFeatured() {
        log.info("Retrieving featured events");
        List<Event> events = eventRepository.findByIsActiveTrueAndIsFeaturedTrue();
        log.info("Found {} featured events", events.size());
        return events;
    }

    @Transactional
    public void updateStatus(Long eventId, EventStatus newStatus) {
        log.info("Updating status for event id: {} to {}", eventId, newStatus);
        if(!eventRepository.existsById(eventId)) {
            log.warn("Event not found with id: {}", eventId);
            throw new ResourceNotFoundException("Not Found Event");
        }
        int updated = eventRepository.updateStatus(eventId, newStatus, auditService.getActualUserId());

        if (updated == 0) {
            log.warn("Failed to update status for event id: {}", eventId);
            throw new EntityNotFoundException("Error updating Event Status");
        }
        log.info("Event status updated successfully for id: {}", eventId);
    }

    @Transactional
    public void cancelEvent(Long eventId) {
        updateStatus(eventId, EventStatus.CANCELLED);
    }

    public List<Event> getEventsByCategoryId(Long categoryId) {
        log.info("Retrieving events for category id: {}", categoryId);
        List<Event> events = eventRepository.findByCategoryId(categoryId);
        log.info("Found {} events for category id: {}", events.size(), categoryId);
        return events;
    }

    @Transactional
    public Event update(Long eventId, EventRequest dto, List<MultipartFile> images) {
        log.info("Updating event with id: {}", eventId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> {
                log.warn("Event not found with id: {}", eventId);
                return new ResourceNotFoundException("Event not found with id: " + eventId);
            });

        if (!event.getOrganizedByUser().getId().equals(currentUser.getId())) {
            log.warn("User id: {} attempted to edit event id: {} without permission", currentUser.getId(), eventId);
            throw new AccessDeniedException("You are not allowed to edit this event");
        }

        if (dto.getTitle() != null)                 event.setTitle(dto.getTitle());
        if (dto.getShortDescription() != null)      event.setShortDescription(dto.getShortDescription());
        if (dto.getDescription() != null)           event.setDescription(dto.getDescription());
        if (dto.getCost() != null)                  event.setCost(dto.getCost());
        if (dto.getLatitude() != null)              event.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null)             event.setLongitude(dto.getLongitude());
        if (dto.getShortPlaceDescription() != null) event.setShortPlaceDescription(dto.getShortPlaceDescription());
        if (dto.getPeopleCapacity() != null)        event.setPeopleCapacity(dto.getPeopleCapacity());
        if (dto.getDateStart() != null)             event.setDateStart(dto.getDateStart());
        if (dto.getDateEnd() != null)               event.setDateEnd(dto.getDateEnd());
        if (dto.getTags() != null)                  event.setTags(dto.getTags());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            event.setCategory(category);
        }

        List<String> updatedPhotos = new java.util.ArrayList<>();

        if (dto.getPhotoLinks() != null) {
            updatedPhotos.addAll(dto.getPhotoLinks());
        }

        if (images != null && !images.isEmpty()) {
            updatedPhotos.addAll(cloudinaryService.uploadImages(images)); // nuevas al final
        }

        if (!updatedPhotos.isEmpty()) {
            event.setPhotoLinks(updatedPhotos);
        }

        event.setModifiedByUserId(currentUser.getId());

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated successfully with id: {}", updatedEvent.getId());
        return updatedEvent;
    }
}
