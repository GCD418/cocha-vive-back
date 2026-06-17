package cocha.vive.backend.controller;

import cocha.vive.backend.core.annotations.FeatureFlag;
import cocha.vive.backend.core.enums.AppFeature;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.EventStatus;
import cocha.vive.backend.model.dto.EventRejectDTO;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.model.dto.EventResponseDTO;
import cocha.vive.backend.service.EventService;
import jakarta.validation.Valid;
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
    public List<EventResponseDTO> getAllEvents(){
        return eventService.toResponseDtoList(eventService.getAllPublic());
    }

    @GetMapping("/events/my-events")
    @PreAuthorize("hasRole('PUBLISHER')")
    public List<EventResponseDTO> getMyEvents() {
        return eventService.toResponseDtoList(eventService.getMyEvents());
    }

    @GetMapping("/events/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EventResponseDTO> getAllEventsForAdmin() {
        return eventService.toResponseDtoList(eventService.getAllForAdmin());
    }

    @PostMapping(value = "/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PUBLISHER')")
    public ResponseEntity<Event> createEvent(@RequestPart("event") EventRequest dto, @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(dto, images));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.toResponseDto(eventService.findById(id)));
    }

    @GetMapping("/events/upcoming")
    public List<EventResponseDTO> getUpcomingEvents(){
        return eventService.toResponseDtoList(eventService.getUpcoming());
    }

    @GetMapping("/events/featured")
    public List<EventResponseDTO> getFeaturedEvents(){
        return eventService.toResponseDtoList(eventService.getFeatured());
    }

    @PatchMapping("/events/{id}/cancel")
    public ResponseEntity<Void> cancelEvent(@PathVariable Long id) {
        eventService.cancelEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/category/{categoryId}")
    public List<EventResponseDTO> getEventsByCategory(@PathVariable Long categoryId) {
        return eventService.toResponseDtoList(eventService.getEventsByCategoryId(categoryId));
    }

    @PutMapping(value = "/events/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PUBLISHER')")
    public ResponseEntity<Event> updateEvent(
        @PathVariable Long id,
        @RequestPart("event") EventRequest dto,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(eventService.update(id, dto, images));
    }

    @PatchMapping("/events/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveEvent(@PathVariable Long id) {
        eventService.updateStatus(id, EventStatus.APPROVED);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/events/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectEvent(@PathVariable Long id,
                                            @RequestBody @Valid EventRejectDTO dto) {
        eventService.rejectEvent(id, dto);
        return ResponseEntity.noContent().build();
    }

}
