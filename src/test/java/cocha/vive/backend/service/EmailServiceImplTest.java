package cocha.vive.backend.service;

import cocha.vive.backend.config.AppEmailProperties;
import cocha.vive.backend.model.EmailAuditLog;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.EmailRequest;
import cocha.vive.backend.repository.EmailAuditLogRepository;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl tests")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private EmailAuditLogRepository emailAuditLogRepository;

    @Mock
    private AppEmailProperties appEmailProperties;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    @DisplayName("sendWelcomeEmail should render template, send html email and persist audit")
    void sendWelcomeEmail_shouldRenderSendAndAudit() throws Exception {
        User recipient = user(10L, "Ana", "Pérez", "ana@mail.com", "ROLE_USER");
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));

        when(appEmailProperties.getFrom()).thenReturn("CochaVive manager <no-reply@cochavive.com>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("emails/welcome"), any(Context.class))).thenReturn("<html>welcome</html>");
        when(emailAuditLogRepository.save(any(EmailAuditLog.class))).thenAnswer(i -> i.getArgument(0));

        emailService.sendWelcomeEmail(recipient);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emails/welcome"), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getVariable("userName")).isEqualTo("Ana Pérez");

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentMessage = messageCaptor.getValue();

        assertThat(((InternetAddress) sentMessage.getFrom()[0]).toString())
            .isEqualTo("CochaVive manager <no-reply@cochavive.com>");
        assertThat(((InternetAddress) sentMessage.getRecipients(Message.RecipientType.TO)[0]).getAddress())
            .isEqualTo("ana@mail.com");
        assertThat(sentMessage.getSubject()).isEqualTo("¡Bienvenido/a a CochaVive!");

        ArgumentCaptor<EmailAuditLog> auditCaptor = ArgumentCaptor.forClass(EmailAuditLog.class);
        verify(emailAuditLogRepository).save(auditCaptor.capture());
        EmailAuditLog auditLog = auditCaptor.getValue();
        assertThat(auditLog.getRecipient()).isEqualTo("ana@mail.com");
        assertThat(auditLog.getSubject()).isEqualTo("¡Bienvenido/a a CochaVive!");
        assertThat(auditLog.getTemplateName()).isEqualTo("emails/welcome");
        assertThat(auditLog.getCreatedByUserId()).isNull();
    }

    @Test
    @DisplayName("sendNewEventWantsToBePublishedEmail should notify admin and audit organizer as creator")
    void sendNewEventWantsToBePublishedEmail_shouldNotifyAdminAndAuditOrganizer() {
        User admin = user(1L, "Admin", "Uno", "admin@mail.com", "ROLE_ADMIN");
        User organizer = user(2L, "Juan", "López", "juan@mail.com", "ROLE_USER");

        Event event = new Event();
        event.setTitle("Festival Cultural");
        event.setDateStart(LocalDateTime.of(2026, 4, 20, 19, 30));
        event.setOrganizedByUser(organizer);

        when(appEmailProperties.getFrom()).thenReturn("no-reply@cochavive.com");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(templateEngine.process(eq("emails/new-event-wants-to-be-published"), any(Context.class)))
            .thenReturn("<html>event</html>");

        emailService.sendNewEventWantsToBePublishedEmail(admin, event);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emails/new-event-wants-to-be-published"), contextCaptor.capture());
        Context context = contextCaptor.getValue();
        assertThat(context.getVariable("adminName")).isEqualTo("Admin Uno");
        assertThat(context.getVariable("eventTitle")).isEqualTo("Festival Cultural");
        assertThat(context.getVariable("organizerName")).isEqualTo("Juan López");

        ArgumentCaptor<EmailAuditLog> auditCaptor = ArgumentCaptor.forClass(EmailAuditLog.class);
        verify(emailAuditLogRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getCreatedByUserId()).isEqualTo(organizer);
    }

    @Test
    @DisplayName("sendCustomEmail should use provided template and body variable")
    void sendCustomEmail_shouldUseTemplateAndBodyVariable() {
        User createdBy = user(99L, "Soporte", "Tech", "support@mail.com", "ROLE_ADMIN");
        EmailRequest request = new EmailRequest("to@mail.com", "Asunto custom", "Mensaje body");

        when(appEmailProperties.getFrom()).thenReturn("no-reply@cochavive.com");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(templateEngine.process(eq("emails/custom"), any(Context.class))).thenReturn("<html>custom</html>");

        emailService.sendCustomEmail(request, "emails/custom", createdBy);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emails/custom"), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getVariable("body")).isEqualTo("Mensaje body");

        ArgumentCaptor<EmailAuditLog> auditCaptor = ArgumentCaptor.forClass(EmailAuditLog.class);
        verify(emailAuditLogRepository).save(auditCaptor.capture());
        EmailAuditLog auditLog = auditCaptor.getValue();
        assertThat(auditLog.getTemplateName()).isEqualTo("emails/custom");
        assertThat(auditLog.getCreatedByUserId()).isEqualTo(createdBy);
    }

    @Test
    @DisplayName("send mail failure should not persist audit and should not throw")
    void sendMailFailure_shouldNotPersistAuditAndShouldNotThrow() {
        User recipient = user(10L, "Ana", "Pérez", "ana@mail.com", "ROLE_USER");

        when(appEmailProperties.getFrom()).thenReturn("no-reply@cochavive.com");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(templateEngine.process(eq("emails/welcome"), any(Context.class))).thenReturn("<html>welcome</html>");
        doThrow(new MailSendException("smtp-down")).when(mailSender).send(any(MimeMessage.class));

        emailService.sendWelcomeEmail(recipient);

        verify(emailAuditLogRepository, never()).save(any(EmailAuditLog.class));
    }

    @Test
    @DisplayName("sendNewConvertToPublisherRequestEmail should fill expected template variables")
    void sendNewConvertToPublisherRequestEmail_shouldFillExpectedVariables() {
        User admin = user(1L, "Admin", "Uno", "admin@mail.com", "ROLE_ADMIN");
        User requester = user(7L, "María", "Sosa", "maria@mail.com", "ROLE_USER");

        PublisherRequest publisherRequest = new PublisherRequest();
        publisherRequest.setCreatedByUserId(requester);
        publisherRequest.setLegalEntityName("Cocha Cultura SRL");
        publisherRequest.setRequestReason("Organizamos eventos semanales");

        when(appEmailProperties.getFrom()).thenReturn("no-reply@cochavive.com");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(templateEngine.process(eq("emails/new-convert-to-publisher-request"), any(Context.class)))
            .thenReturn("<html>publisher</html>");

        emailService.sendNewConvertToPublisherRequestEmail(admin, publisherRequest);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emails/new-convert-to-publisher-request"), contextCaptor.capture());

        Context context = contextCaptor.getValue();
        assertThat(context.getVariable("adminName")).isEqualTo("Admin Uno");
        assertThat(context.getVariable("requesterName")).isEqualTo("María Sosa");
        assertThat(context.getVariable("legalEntityName")).isEqualTo("Cocha Cultura SRL");
        assertThat(context.getVariable("requestReason")).isEqualTo("Organizamos eventos semanales");
    }

    @Test
    @DisplayName("null arguments should fail fast")
    void nullArguments_shouldFailFast() {
        assertThrows(NullPointerException.class, () -> emailService.sendWelcomeEmail(null));
        assertThrows(NullPointerException.class, () -> emailService.sendCustomEmail(null, "emails/custom", null));
        assertThrows(NullPointerException.class, () -> emailService.sendCustomEmail(
            new EmailRequest("test@mail.com", "s", "b"), null, null));
    }

    private User user(Long id, String names, String firstLastName, String email, String role) {
        User user = new User();
        user.setId(id);
        user.setNames(names);
        user.setFirstLastName(firstLastName);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
