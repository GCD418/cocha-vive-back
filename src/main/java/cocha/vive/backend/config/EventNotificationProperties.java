package cocha.vive.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.notifications.event")
public class EventNotificationProperties {

    private String rejectedTitle;
    private String rejectedDescription;
}
