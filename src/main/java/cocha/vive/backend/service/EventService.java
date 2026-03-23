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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    public List<Event> getAll(){
        return eventRepository.findAll();
    }

    public Event create(EventRequest dto, List<MultipartFile> images) {
        User user = userRepository.findById(dto.getOrganizedByUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

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

        return eventRepository.save(event);
    }

    public Event findById(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event not exists with id: " + id));
    }

    public List<Event> getUpcoming() {
        return eventRepository.findActiveUpcoming();
    }

    public List<Event> getFeatured() {
        return eventRepository.findByIsActiveTrueAndIsFeaturedTrue();
    }

    @Transactional
    public void updateStatus(Long eventId, EventStatus newStatus) {
        if(!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Not Found Event");
        }
        int updated = eventRepository.updateStatus(eventId, newStatus, 1L);

        if (updated == 0) {
            throw new EntityNotFoundException("Error updating Event Status");
        }
    }

    @Transactional
    public void cancelEvent(Long eventId) {
        updateStatus(eventId, EventStatus.CANCELLED);
    }
    public List<Event> getEventsByCategoryId(Long categoryId) { return eventRepository.findByCategoryId(categoryId); }

    @Transactional
    public Event update(Long eventId, EventRequest dto, List<MultipartFile> images) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (!event.getOrganizedByUser().getId().equals(currentUser.getId())) {
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

        if (images != null && !images.isEmpty()) {
            event.setPhotoLinks(cloudinaryService.uploadImages(images));
        }

        event.setModifiedByUserId(currentUser.getId());

        return eventRepository.save(event);
    }
}
