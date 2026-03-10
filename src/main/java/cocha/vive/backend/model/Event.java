package cocha.vive.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    private long id;
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private double cost;
}
