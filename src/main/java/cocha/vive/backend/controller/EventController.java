package cocha.vive.backend.controller;

import cocha.vive.backend.model.Event;
import cocha.vive.backend.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
