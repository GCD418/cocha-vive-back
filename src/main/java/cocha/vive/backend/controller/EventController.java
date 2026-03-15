package cocha.vive.backend.controller;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.EventStatus;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.repository.CategoryRepository;
import cocha.vive.backend.repository.EventRepository;
import cocha.vive.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {

    @Autowired
    public EventRepository eventRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public CategoryRepository categoryRepository;

    @GetMapping("/events")
    public List<Event> getAllEvents(){
        return eventRepository.findAll();
    }

    @PostMapping("/events")
    public Event createEvent(@RequestBody EventRequest dto) {
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
            .photoLinks(dto.getPhotoLinks())
            .eventStatus(EventStatus.APPROVED)
            .isActive(true)
            .build();

        return eventRepository.save(event);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() ->new ResourceNotFoundException("Event not exists with id: "+id));
        return ResponseEntity.ok(event);
    }

    @GetMapping("/events/upcoming")
    public List<Event> getUpcomingEvents(){
        return eventRepository.findActiveUpcoming();
    }

    @GetMapping("/events/featured")
    public List<Event> getFeaturedEvents(){
        return eventRepository.findByIsActiveTrueAndIsFeaturedTrue();
    }

}
