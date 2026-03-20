package cocha.vive.backend.controller;

import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping("/events")
    public List<Event> getAllEvents(){
        return eventService.getAll();
    }

    @PostMapping(value = "/events")
    public ResponseEntity<Event> createEvent(@RequestPart("event") EventRequest dto, @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(dto, images));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @GetMapping("/events/upcoming")
    public List<Event> getUpcomingEvents(){
        return eventService.getUpcoming();
    }

    @GetMapping("/events/featured")
    public List<Event> getFeaturedEvents(){
        return eventService.getFeatured();
    }

    @GetMapping("/events/category/{categoryId}")
    public List<Event> getEventsByCategory(@PathVariable Long categoryId) {
        return eventService.getEventsByCategoryId(categoryId);
    }

}
