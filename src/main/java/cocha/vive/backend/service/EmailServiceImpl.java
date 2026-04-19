package cocha.vive.backend.service;

import cocha.vive.backend.config.AppEmailProperties;
import cocha.vive.backend.model.EmailAuditLog;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.EmailRequest;
import cocha.vive.backend.repository.EmailAuditLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailAuditLogRepository emailAuditLogRepository;
    private final AppEmailProperties appEmailProperties;

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
}
