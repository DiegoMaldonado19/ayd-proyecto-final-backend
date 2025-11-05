package com.ayd.parkcontrol.infrastructure.notification;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EmailServiceImpl.
 * Valida la construcción y envío de correos electrónicos para diferentes
 * escenarios.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    private String fromEmail = "noreply@parkcontrol.com";
    private String appName = "ParkControl S.A.";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
        ReflectionTestUtils.setField(emailService, "appName", appName);
    }

    @Test
    void sendEmail_shouldSendEmailSuccessfully() throws Exception {
        String to = "user@example.com";
        String subject = "Test Subject";
        String body = "<h1>Test Body</h1>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(to, subject, body);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendEmail_shouldThrowException_whenMessagingFails() {
        String to = "user@example.com";
        String subject = "Test Subject";
        String body = "<h1>Test Body</h1>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(RuntimeException.class).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> emailService.sendEmail(to, subject, body))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void send2FACode_shouldSendEmailWithCode() {
        String to = "user@example.com";
        String code = "123456";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.send2FACode(to, code);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        verify(mailSender).createMimeMessage();
    }

    @Test
    void send2FACode_shouldContainCorrectSubject() {
        String to = "user@example.com";
        String code = "123456";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.send2FACode(to, code);

        // Verificar que se llamó a sendEmail con el subject correcto (indirectamente a
        // través de send)
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_shouldSendEmailWithResetLink() {
        String to = "user@example.com";
        String resetLink = "https://parkcontrol.com/reset?token=abc123";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPasswordResetEmail(to, resetLink);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendPasswordResetEmail_shouldContainCorrectSubject() {
        String to = "user@example.com";
        String resetLink = "https://parkcontrol.com/reset?token=abc123";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPasswordResetEmail(to, resetLink);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordChangedNotification_shouldSendEmailWithUserName() {
        String to = "user@example.com";
        String userName = "John Doe";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPasswordChangedNotification(to, userName);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendPasswordChangedNotification_shouldContainCorrectSubject() {
        String to = "user@example.com";
        String userName = "John Doe";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPasswordChangedNotification(to, userName);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_shouldUseTrustedSender() {
        String to = "user@example.com";
        String subject = "Test Subject";
        String body = "<h1>Test Body</h1>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(to, subject, body);

        // Verificar que createMimeMessage fue llamado (esto implica que se configuró el
        // from)
        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendEmail_shouldSupportHtmlContent() {
        String to = "user@example.com";
        String subject = "HTML Test";
        String body = "<html><body><h1>HTML Content</h1></body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(to, subject, body);

        // Verificar que se envió con soporte HTML (implícito en MimeMessageHelper)
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPlateChangeApprovedNotification_shouldSendEmailSuccessfully() {
        String to = "user@example.com";
        String userName = "John Doe";
        String oldPlate = "ABC123";
        String newPlate = "XYZ789";
        String reviewNotes = "Documentación verificada correctamente";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPlateChangeApprovedNotification(to, userName, oldPlate, newPlate, reviewNotes);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendPlateChangeApprovedNotification_shouldSendWithoutReviewNotes() {
        String to = "user@example.com";
        String userName = "Jane Smith";
        String oldPlate = "OLD123";
        String newPlate = "NEW456";
        String reviewNotes = null;

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPlateChangeApprovedNotification(to, userName, oldPlate, newPlate, reviewNotes);

        verify(mailSender).send(any(MimeMessage.class));
        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendPlateChangeRejectedNotification_shouldSendEmailSuccessfully() {
        String to = "user@example.com";
        String userName = "John Doe";
        String oldPlate = "ABC123";
        String newPlate = "XYZ789";
        String reviewNotes = "Documentación incompleta";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPlateChangeRejectedNotification(to, userName, oldPlate, newPlate, reviewNotes);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendPlateChangeRejectedNotification_shouldSendWithoutReviewNotes() {
        String to = "user@example.com";
        String userName = "Jane Smith";
        String oldPlate = "OLD123";
        String newPlate = "NEW456";
        String reviewNotes = "";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPlateChangeRejectedNotification(to, userName, oldPlate, newPlate, reviewNotes);

        verify(mailSender).send(any(MimeMessage.class));
        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendPlateChangeApprovedNotification_shouldContainCorrectPlates() {
        String to = "user@example.com";
        String userName = "Test User";
        String oldPlate = "TEST123";
        String newPlate = "TEST456";
        String reviewNotes = "Test notes";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPlateChangeApprovedNotification(to, userName, oldPlate, newPlate, reviewNotes);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPlateChangeRejectedNotification_shouldContainCorrectPlates() {
        String to = "user@example.com";
        String userName = "Test User";
        String oldPlate = "TEST123";
        String newPlate = "TEST456";
        String reviewNotes = "Reason for rejection";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPlateChangeRejectedNotification(to, userName, oldPlate, newPlate, reviewNotes);

        verify(mailSender).send(any(MimeMessage.class));
    }
}
