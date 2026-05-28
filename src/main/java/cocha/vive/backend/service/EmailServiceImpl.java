package cocha.vive.backend.service;

import cocha.vive.backend.config.AppEmailProperties;
import cocha.vive.backend.config.AppProperties;
import cocha.vive.backend.model.EmailAuditLog;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.Ticket;
import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.EmailRequest;
import cocha.vive.backend.repository.EmailAuditLogRepository;
import cocha.vive.backend.model.EventPromotion;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String TEMPLATE_WELCOME = "emails/welcome";
    private static final String TEMPLATE_NEW_EVENT_TO_PUBLISH = "emails/new-event-wants-to-be-published";
    private static final String TEMPLATE_NEW_PUBLISHER_REQUEST = "emails/new-convert-to-publisher-request";
    private static final String TEMPLATE_PUBLISHER_REQUEST_APPROVED = "emails/publisher-request-approved";
    private static final String TEMPLATE_PUBLISHER_REQUEST_REJECTED = "emails/publisher-request-rejected";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String TEMPLATE_EMAIL_VERIFICATION = "emails/email-verification";
    private static final String TEMPLATE_PUBLISHER_DEMOTED = "emails/publisher-demoted";
    private static final String TEMPLATE_TICKET_PURCHASED = "emails/ticket-purchased";
    private static final String TEMPLATE_EVENT_PROMOTED = "emails/event-promoted";
    private static final String TEMPLATE_EVENT_REJECTED = "emails/event-rejected";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailAuditLogRepository emailAuditLogRepository;
    private final AppEmailProperties appEmailProperties;
    private final AppProperties appProperties;

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendWelcomeEmail(User recipientUser) {
        Objects.requireNonNull(recipientUser, "recipientUser must not be null");

        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", resolveUserDisplayName(recipientUser));

        EmailRequest request = new EmailRequest(
            recipientUser.getEmail(),
            "¡Bienvenido/a a CochaVive!",
            ""
        );

        sendTemplatedEmail(request, TEMPLATE_WELCOME, variables, null);
    }

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNewEventWantsToBePublishedEmail(User adminRecipient, Event event) {
        Objects.requireNonNull(adminRecipient, "adminRecipient must not be null");
        Objects.requireNonNull(event, "event must not be null");

        Map<String, Object> variables = new HashMap<>();
        variables.put("adminName", resolveUserDisplayName(adminRecipient));
        variables.put("eventTitle", event.getTitle());
        variables.put("eventDateStart", event.getDateStart() != null ? DATE_TIME_FORMATTER.format(event.getDateStart()) : "No definido");
        variables.put("organizerName", event.getOrganizedByUser() != null ? resolveUserDisplayName(event.getOrganizedByUser()) : "Organizador desconocido");
        variables.put("eventCoverImageUrl", resolveFirstPhotoUrl(event.getPhotoLinks()));

        EmailRequest request = new EmailRequest(
            adminRecipient.getEmail(),
            "Nuevo evento pendiente de publicación",
            ""
        );

        User createdBy = event.getOrganizedByUser();
        sendTemplatedEmail(request, TEMPLATE_NEW_EVENT_TO_PUBLISH, variables, createdBy);
    }

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNewConvertToPublisherRequestEmail(User adminRecipient, PublisherRequest publisherRequest) {
        Objects.requireNonNull(adminRecipient, "adminRecipient must not be null");
        Objects.requireNonNull(publisherRequest, "publisherRequest must not be null");

        Map<String, Object> variables = new HashMap<>();
        variables.put("adminName", resolveUserDisplayName(adminRecipient));
        variables.put("requesterName", publisherRequest.getCreatedByUserId() != null
            ? resolveUserDisplayName(publisherRequest.getCreatedByUserId())
            : "Usuario desconocido");
        variables.put("legalEntityName", publisherRequest.getLegalEntityName());
        variables.put("requestReason", publisherRequest.getRequestReason());

        EmailRequest request = new EmailRequest(
            adminRecipient.getEmail(),
            "Nueva solicitud para convertirse en publisher",
            ""
        );

        sendTemplatedEmail(request, TEMPLATE_NEW_PUBLISHER_REQUEST, variables, publisherRequest.getCreatedByUserId());
    }

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendPublisherRequestApprovedEmail(User recipientUser, PublisherRequest publisherRequest) {
        Objects.requireNonNull(recipientUser, "recipientUser must not be null");
        Objects.requireNonNull(publisherRequest, "publisherRequest must not be null");

        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", resolveUserDisplayName(recipientUser));
        variables.put("legalEntityName", publisherRequest.getLegalEntityName());

        EmailRequest request = new EmailRequest(
            recipientUser.getEmail(),
            "Solicitud aprobada",
            ""
        );

        sendTemplatedEmail(request, TEMPLATE_PUBLISHER_REQUEST_APPROVED, variables, recipientUser);
    }

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendPublisherRequestRejectedEmail(User recipientUser, PublisherRequest publisherRequest) {
        Objects.requireNonNull(recipientUser, "recipientUser must not be null");
        Objects.requireNonNull(publisherRequest, "publisherRequest must not be null");

        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", resolveUserDisplayName(recipientUser));
        variables.put("legalEntityName", publisherRequest.getLegalEntityName());
        variables.put("rejectionReason", publisherRequest.getRejectionReason());

        EmailRequest request = new EmailRequest(
            recipientUser.getEmail(),
            "Solicitud rechazada",
            ""
        );

        sendTemplatedEmail(request, TEMPLATE_PUBLISHER_REQUEST_REJECTED, variables, recipientUser);
    }

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEventRejectedEmail(User recipientUser, Event event) {
        Objects.requireNonNull(recipientUser, "recipientUser must not be null");
        Objects.requireNonNull(event, "event must not be null");

        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", resolveUserDisplayName(recipientUser));
        variables.put("eventTitle", event.getTitle());
        variables.put("rejectionReason", event.getRejectionReason());

        EmailRequest request = new EmailRequest(
            recipientUser.getEmail(),
            "Evento rechazado",
            ""
        );

        sendTemplatedEmail(request, TEMPLATE_EVENT_REJECTED, variables, recipientUser);
    }

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendPublisherDemotionEmail(User recipientUser, String demotionReason) {
        Objects.requireNonNull(recipientUser, "recipientUser must not be null");
        Objects.requireNonNull(demotionReason, "demotionReason must not be null");

        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", resolveUserDisplayName(recipientUser));
        variables.put("demotionReason", demotionReason);

        EmailRequest request = new EmailRequest(
            recipientUser.getEmail(),
            "Tu acceso como publisher ha sido revocado",
            ""
        );

        sendTemplatedEmail(request, TEMPLATE_PUBLISHER_DEMOTED, variables, recipientUser);
    }

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendCustomEmail(EmailRequest request, String templateName, User createdByUser) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(templateName, "templateName must not be null");
        sendTemplatedEmail(request, templateName, Map.of("body", request.body()), createdByUser);
    }

    private void sendTemplatedEmail(EmailRequest request,
                                    String templateName,
                                    Map<String, Object> variables,
                                    User createdByUser) {
        try {
            String htmlBody = renderTemplate(templateName, variables);
            sendHtmlEmail(request.to(), request.subject(), htmlBody);
            persistAuditLog(request.to(), request.subject(), templateName, createdByUser);
            log.info("Email queued and audit log stored. to={}, subject={}, template={}", request.to(), request.subject(), templateName);
        } catch (MailException | MessagingException e) {
            log.error("Error sending email asynchronously. to={}, subject={}, template={}", request.to(), request.subject(), templateName, e);
        } catch (Exception e) {
            log.error("Unexpected error preparing async email. to={}, subject={}, template={}", request.to(), request.subject(), templateName, e);
        }
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context(Locale.forLanguageTag("es-BO"));
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(appEmailProperties.getFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    private void persistAuditLog(String recipient, String subject, String templateName, User createdByUser) {
        EmailAuditLog auditLog = new EmailAuditLog();
        auditLog.setRecipient(recipient);
        auditLog.setSubject(subject);
        auditLog.setTemplateName(templateName);
        auditLog.setCreatedByUserId(createdByUser);
        emailAuditLogRepository.save(auditLog);
    }

    private String resolveUserDisplayName(User user) {
        if (user == null) {
            return "";
        }

        StringBuilder displayName = new StringBuilder();
        if (user.getNames() != null && !user.getNames().isBlank()) {
            displayName.append(user.getNames().trim());
        }
        if (user.getFirstLastName() != null && !user.getFirstLastName().isBlank()) {
            if (displayName.length() > 0) {
                displayName.append(' ');
            }
            displayName.append(user.getFirstLastName().trim());
        }

        if (displayName.length() > 0) {
            return displayName.toString();
        }
        return user.getEmail() != null ? user.getEmail() : "Usuario";
    }

    private String resolveFirstPhotoUrl(List<String> photoLinks) {
        if (photoLinks == null || photoLinks.isEmpty()) {
            return null;
        }

        return photoLinks.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(link -> !link.isBlank())
            .findFirst()
            .orElse(null);
    }

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEmailVerificationEmail(String recipientEmail, String verificationToken) {
        Objects.requireNonNull(recipientEmail, "recipientEmail must not be null");
        Objects.requireNonNull(verificationToken, "verificationToken must not be null");

        String verificationLink = appProperties.getFrontendUrl()
            + "/facebook/verify-email?token="
            + verificationToken;

        Map<String, Object> variables = new HashMap<>();
        variables.put("verificationLink", verificationLink);

        EmailRequest request = new EmailRequest(
            recipientEmail,
            "Verifica tu email en CochaVive",
            ""
        );

        sendTemplatedEmail(request, TEMPLATE_EMAIL_VERIFICATION, variables, null);
    }

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendTicketPurchasedEmail(User recipientUser, Ticket ticket, byte[] qrCodePng) {
        Objects.requireNonNull(recipientUser, "recipientUser must not be null");
        Objects.requireNonNull(ticket, "ticket must not be null");
        Objects.requireNonNull(qrCodePng, "qrCodePng must not be null");

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", resolveUserDisplayName(recipientUser));
            variables.put("quantity", ticket.getQuantity());
            variables.put("unitPrice", formatMoneyFromMinor(ticket.getUnitPrice()));
            variables.put("totalPrice", formatMoneyFromMinor(ticket.totalPrice()));
            variables.put("expired", ticket.isExpired());
            variables.put("eventTitle", ticket.getEvent() != null ? ticket.getEvent().getTitle() : "Evento");
            variables.put("eventDateStart", ticket.getEvent() != null && ticket.getEvent().getDateStart() != null
                ? DATE_TIME_FORMATTER.format(ticket.getEvent().getDateStart())
                : "No definido");

            String htmlBody = renderTemplate(TEMPLATE_TICKET_PURCHASED, variables);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(appEmailProperties.getFrom());
            helper.setTo(recipientUser.getEmail());
            helper.setSubject("Tu ticket CochaVive");
            helper.setText(htmlBody, true);

            // Many email clients block data URIs; CID inline is more compatible.
            helper.addInline("ticketQr", new ByteArrayResource(qrCodePng), "image/png");

            mailSender.send(message);
            persistAuditLog(recipientUser.getEmail(), "Tu ticket CochaVive", TEMPLATE_TICKET_PURCHASED, ticket.getBuyerUserId());
            log.info("Ticket email queued and audit log stored. to={}, template={}", recipientUser.getEmail(), TEMPLATE_TICKET_PURCHASED);
        } catch (MailException | MessagingException e) {
            log.error("Error sending ticket email asynchronously. to={}, template={}", recipientUser.getEmail(), TEMPLATE_TICKET_PURCHASED, e);
        } catch (Exception e) {
            log.error("Unexpected error preparing ticket email. to={}, template={}", recipientUser.getEmail(), TEMPLATE_TICKET_PURCHASED, e);
        }
    }

    @Override
    @Async("emailExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEventPromotedEmail(User recipientUser, String eventTitle, String planName,
                                   Long amount, String startAt, String endAt, byte[] qrCodePng) {
        Objects.requireNonNull(recipientUser, "recipientUser must not be null");

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", resolveUserDisplayName(recipientUser));
            variables.put("eventTitle", eventTitle);
            variables.put("planName", planName);
            variables.put("amount", formatPromotionAmount(amount));
            variables.put("startAt", startAt);
            variables.put("endAt", endAt);

            String htmlBody = renderTemplate(TEMPLATE_EVENT_PROMOTED, variables);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(appEmailProperties.getFrom());
            helper.setTo(recipientUser.getEmail());
            helper.setSubject("Tu evento destacado en CochaVive");
            helper.setText(htmlBody, true);

            // Many email clients block data URIs; CID inline is more compatible.
            helper.addInline("promotionQr", new ByteArrayResource(qrCodePng), "image/png");

            mailSender.send(message);
            persistAuditLog(recipientUser.getEmail(), "Tu evento destacado en CochaVive",
                TEMPLATE_EVENT_PROMOTED, recipientUser);
            log.info("Promotion email queued and audit log stored. to={}, template={}",
                recipientUser.getEmail(), TEMPLATE_EVENT_PROMOTED);
        } catch (MailException | MessagingException e) {
            log.error("Error sending promotion email asynchronously. to={}, template={}",
                recipientUser.getEmail(), TEMPLATE_EVENT_PROMOTED, e);
        } catch (Exception e) {
            log.error("Unexpected error preparing promotion email. to={}, template={}",
                recipientUser.getEmail(), TEMPLATE_EVENT_PROMOTED, e);
        }
    }

    /**
     * Formats a promotion amount stored in major currency units (Option A).
     * Unlike {@link #formatMoneyFromMinor}, this does NOT divide by 100.
     */
    private String formatPromotionAmount(Long amount) {
        if (amount == null) {
            return "-";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-BO"));
        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        return "Bs " + format.format(amount);
    }

    /**
     * Formats a minor-unit money amount (e.g., stored as value * 100) for display.
     */
    private String formatMoneyFromMinor(Long minorAmount) {
        if (minorAmount == null) {
            return "-";
        }
        BigDecimal value = BigDecimal.valueOf(minorAmount, 2);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-BO"));
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return "Bs " + format.format(value);
    }
}
