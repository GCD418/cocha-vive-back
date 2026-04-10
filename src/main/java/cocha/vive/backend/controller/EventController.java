package cocha.vive.backend.controller;

import cocha.vive.backend.core.annotations.FeatureFlag;
import cocha.vive.backend.core.enums.AppFeature;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping("/events")
    public List<Event> getAllEvents(){
        return eventService.getAllPublic();
    }

    @GetMapping("/events/my-events")
    @PreAuthorize("hasRole('PUBLISHER')")
    public List<Event> getMyEvents() {
        return eventService.getMyEvents();
    }

    @GetMapping("/events/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Event> getAllEventsForAdmin() {
        return eventService.getAllForAdmin();
    }

    @PostMapping(value = "/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PUBLISHER')")
    public ResponseEntity<Event> createEvent(@RequestPart("event") EventRequest dto, @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(dto, images));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @GetMapping("/events/upcoming")
    @FeatureFlag(AppFeature.VIEW_UPCOMING_EVENTS)
    public List<Event> getUpcomingEvents(){
        return eventService.getUpcoming();
    }

    @GetMapping("/events/featured")
    @FeatureFlag(AppFeature.VIEW_FEATURED_EVENTS)
    public List<Event> getFeaturedEvents(){
        return eventService.getFeatured();
    }

    @PatchMapping("/events/{id}/cancel")
    public ResponseEntity<Void> cancelEvent(@PathVariable Long id) {
        eventService.cancelEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/category/{categoryId}")
    public List<Event> getEventsByCategory(@PathVariable Long categoryId) {
        return eventService.getEventsByCategoryId(categoryId);
    }

    @PutMapping(value = "/events/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PUBLISHER')")
    public ResponseEntity<Event> updateEvent(
        @PathVariable Long id,
        @RequestPart("event") EventRequest dto,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(eventService.update(id, dto, images));
    }

}
