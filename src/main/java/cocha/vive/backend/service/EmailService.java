package cocha.vive.backend.service;

import cocha.vive.backend.model.*;
import cocha.vive.backend.model.dto.EmailRequest;

public interface EmailService {

    void sendWelcomeEmail(User recipientUser);

    void sendNewEventWantsToBePublishedEmail(User adminRecipient, Event event);

    void sendNewConvertToPublisherRequestEmail(User adminRecipient, PublisherRequest publisherRequest);

    void sendPublisherRequestApprovedEmail(User recipientUser, PublisherRequest publisherRequest);

    void sendPublisherRequestRejectedEmail(User recipientUser, PublisherRequest publisherRequest);

    void sendCustomEmail(EmailRequest request, String templateName, User createdByUser);

    void sendPublisherDemotionEmail(User recipientUser, String demotionReason);

    void sendEmailVerificationEmail(String recipientEmail, String verificationToken);

    void sendTicketPurchasedEmail(User recipientUser, Ticket ticket, byte[] qrCodePng);

    void sendEventPromotedEmail(User recipientUser, String eventTitle, String planName,
                            Long amount, String startAt, String endAt, byte[] qrCodePng);
}
