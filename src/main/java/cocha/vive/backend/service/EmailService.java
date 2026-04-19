package cocha.vive.backend.service;

import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.EmailRequest;

public interface EmailService {

    void sendWelcomeEmail(User recipientUser);

    void sendNewEventWantsToBePublishedEmail(User adminRecipient, Event event);

    void sendNewConvertToPublisherRequestEmail(User adminRecipient, PublisherRequest publisherRequest);

    void sendCustomEmail(EmailRequest request, String templateName, User createdByUser);
}
