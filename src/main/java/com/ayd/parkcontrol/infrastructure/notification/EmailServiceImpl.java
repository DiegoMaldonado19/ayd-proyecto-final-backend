package com.ayd.parkcontrol.infrastructure.notification;

import com.ayd.parkcontrol.application.port.notification.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${info.app.name:ParkControl S.A.}")
    private String appName;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Error sending email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void send2FACode(String to, String code) {
        String subject = appName + " - Código de Autenticación de Dos Factores";
        String body = build2FAEmailBody(code);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String to, String code) {
        String subject = appName + " - Solicitud de Restablecimiento de Contraseña";
        String body = buildPasswordResetEmailBody(code);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendPasswordChangedNotification(String to, String userName) {
        String subject = appName + " - Contraseña Cambiada Exitosamente";
        String body = buildPasswordChangedEmailBody(userName);
        sendEmail(to, subject, body);
    }

    private String build2FAEmailBody(String code) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; margin-top: 20px; }
                        .code { font-size: 32px; font-weight: bold; color: #2c3e50; text-align: center; letter-spacing: 5px; padding: 20px; background-color: #ecf0f1; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #7f8c8d; }
                        .warning { color: #e74c3c; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            <h2>Autenticación de Dos Factores</h2>
                            <p>Has solicitado iniciar sesión en tu cuenta. Por favor, utiliza el siguiente código de verificación:</p>
                            <div class="code">%s</div>
                            <p>Este código expirará en <strong>5 minutos</strong>.</p>
                            <p class="warning">Si no solicitaste este código, por favor ignora este correo y asegúrate de que tu cuenta esté segura.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un mensaje automatizado, por favor no responder a este correo.</p>
                            <p>&copy; 2025 %s. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(appName, code, appName);
    }

    private String buildPasswordResetEmailBody(String code) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; margin-top: 20px; }
                        .code { font-size: 32px; font-weight: bold; color: #2c3e50; text-align: center; letter-spacing: 5px; padding: 20px; background-color: #ecf0f1; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #7f8c8d; }
                        .warning { color: #e74c3c; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            <h2>Solicitud de Restablecimiento de Contraseña</h2>
                            <p>Hemos recibido una solicitud para restablecer tu contraseña. Utiliza el siguiente código de verificación:</p>
                            <div class="code">%s</div>
                            <p>Este código expirará en <strong>15 minutos</strong>.</p>
                            <p>Utiliza este código en la aplicación para continuar con el proceso de cambio de contraseña.</p>
                            <p class="warning">Si no solicitaste restablecer tu contraseña, por favor ignora este correo y contacta a soporte si tienes dudas.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un mensaje automatizado, por favor no responder a este correo.</p>
                            <p>&copy; 2025 %s. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(appName, code, appName);
    }

    private String buildPasswordChangedEmailBody(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; margin-top: 20px; }
                        .success { color: #27ae60; font-weight: bold; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #7f8c8d; }
                        .warning { color: #e74c3c; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            <h2 class="success">Contraseña Cambiada Exitosamente</h2>
                            <p>Hola %s,</p>
                            <p>Tu contraseña ha sido cambiada exitosamente.</p>
                            <p>Si realizaste este cambio, no se requiere ninguna acción adicional.</p>
                            <p class="warning">Si no cambiaste tu contraseña, por favor contacta a soporte inmediatamente.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un mensaje automatizado, por favor no responder a este correo.</p>
                            <p>&copy; 2025 %s. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(appName, userName, appName);
    }
}
