package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.EventStatus;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.model.mapper.EventMapper;
import cocha.vive.backend.repository.CategoryRepository;
import cocha.vive.backend.repository.EventRepository;
import cocha.vive.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final AuditService auditService;
    private final EventMapper eventMapper;

    public List<Event> getAll(){
        log.debug("Retrieving all events");
        List<Event> events = eventRepository.findAll();
        log.debug("Retrieved {} events", events.size());
        return events;
    }

    public Event create(EventRequest dto, List<MultipartFile> images) {
        log.info("Creating event with title: {}", dto.getTitle());
        Long actualUserId = auditService.getActualUserId();
        User user = userRepository.findById(actualUserId)
            .orElseThrow(() -> {
                log.warn("User not found with id: {}", actualUserId);
                return new ResourceNotFoundException("Usuario no encontrado");
            });
        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> {
                log.warn("Category not found with id: {}", dto.getCategoryId());
                return new ResourceNotFoundException("Categoría no encontrada");
            });

        Event event = eventMapper.toEntity(dto, category, user, cloudinaryService.uploadImages(images));

        Event savedEvent = eventRepository.save(event);
        log.info("Event created with id: {}", savedEvent.getId());

        return savedEvent;
    }

    public Event findById(Long id) {
        log.debug("Searching event with id: {}", id);
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Event not found with id: {}", id);
                return new ResourceNotFoundException("Event not exists with id: " + id);
            });
        log.debug("Found event with id: {}", event.getId());
        return event;
    }

    public List<Event> getUpcoming() {
        log.debug("Retrieving upcoming events");
        List<Event> events = eventRepository.findActiveUpcoming();
        log.debug("Retrieved {} upcoming events", events.size());
        return events;
    }

    public List<Event> getFeatured() {
        log.debug("Retrieving featured events");
        List<Event> events = eventRepository.findByIsActiveTrueAndIsFeaturedTrue();
        log.debug("Found {} featured events", events.size());
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
        log.info("Cancelling event with id: {}", eventId);
        updateStatus(eventId, EventStatus.CANCELLED);
    }

    public List<Event> getEventsByCategoryId(Long categoryId) {
        log.debug("Retrieving events for category id: {}", categoryId);
        List<Event> events = eventRepository.findByCategoryId(categoryId);
        log.debug("Found {} events for category id: {}", events.size(), categoryId);
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

        eventMapper.updateEventFromRequest(dto, event);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category not found with id: {}", dto.getCategoryId());
                    return new ResourceNotFoundException("Category not found");
                });
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
