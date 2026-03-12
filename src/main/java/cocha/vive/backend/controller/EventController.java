package cocha.vive.backend.controller;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {

    @Autowired
    public EventRepository eventRepository;

    @GetMapping("/events")
    public List<Event> getAllEvents(){
        return eventRepository.findAll();
    }

    @PostMapping("/events")
    public Event createEvent(@RequestBody Event event) {
        return eventRepository.save(event);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() ->new ResourceNotFoundException("Event not exists with id: "+id));
        return ResponseEntity.ok(event);
    }

}
