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

    @Override
    public void sendPlateChangeApprovedNotification(String to, String userName, String oldPlate, String newPlate, String reviewNotes) {
        String subject = appName + " - Cambio de Placa Aprobado";
        String body = buildPlateChangeApprovedEmailBody(userName, oldPlate, newPlate, reviewNotes);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendPlateChangeRejectedNotification(String to, String userName, String oldPlate, String newPlate, String reviewNotes) {
        String subject = appName + " - Cambio de Placa Rechazado";
        String body = buildPlateChangeRejectedEmailBody(userName, oldPlate, newPlate, reviewNotes);
        sendEmail(to, subject, body);
    }

    private String buildPlateChangeApprovedEmailBody(String userName, String oldPlate, String newPlate, String reviewNotes) {
        String notesSection = (reviewNotes != null && !reviewNotes.isBlank()) 
            ? "<p><strong>Notas del revisor:</strong><br/>" + reviewNotes + "</p>"
            : "";
        
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #27ae60; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; margin-top: 20px; }
                        .success { color: #27ae60; font-weight: bold; font-size: 24px; }
                        .plate-box { background-color: #ecf0f1; padding: 15px; border-radius: 5px; margin: 15px 0; border-left: 4px solid #27ae60; }
                        .plate { font-size: 20px; font-weight: bold; color: #2c3e50; font-family: 'Courier New', monospace; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #7f8c8d; }
                        .info-box { background-color: #d5f4e6; padding: 15px; border-radius: 5px; margin: 15px 0; border-left: 4px solid #27ae60; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✓ Solicitud Aprobada</h1>
                        </div>
                        <div class="content">
                            <p class="success">¡Tu cambio de placa ha sido aprobado!</p>
                            <p>Hola %s,</p>
                            <p>Te informamos que tu solicitud de cambio de placa ha sido <strong>aprobada</strong> por nuestro equipo de Back Office.</p>
                            
                            <div class="plate-box">
                                <p><strong>Placa anterior:</strong> <span class="plate">%s</span></p>
                                <p><strong>Nueva placa:</strong> <span class="plate">%s</span></p>
                            </div>
                            
                            %s
                            
                            <div class="info-box">
                                <p><strong>Información importante:</strong></p>
                                <ul>
                                    <li>El cambio es efectivo a partir de este momento</li>
                                    <li>Tu suscripción ahora está asociada a la nueva placa</li>
                                    <li>La placa anterior ya no podrá utilizarse con esta suscripción</li>
                                    <li>Puedes usar tu suscripción en cualquiera de nuestras sucursales</li>
                                </ul>
                            </div>
                            
                            <p>Si tienes alguna duda, no dudes en contactarnos.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un mensaje automatizado, por favor no responder a este correo.</p>
                            <p>&copy; 2025 %s. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(userName, oldPlate, newPlate, notesSection, appName);
    }

    private String buildPlateChangeRejectedEmailBody(String userName, String oldPlate, String newPlate, String reviewNotes) {
        String notesSection = (reviewNotes != null && !reviewNotes.isBlank()) 
            ? "<p><strong>Motivo del rechazo:</strong><br/>" + reviewNotes + "</p>"
            : "<p><strong>Motivo del rechazo:</strong> No se proporcionaron detalles adicionales.</p>";
        
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #e74c3c; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; margin-top: 20px; }
                        .rejected { color: #e74c3c; font-weight: bold; font-size: 24px; }
                        .plate-box { background-color: #ecf0f1; padding: 15px; border-radius: 5px; margin: 15px 0; border-left: 4px solid #e74c3c; }
                        .plate { font-size: 20px; font-weight: bold; color: #2c3e50; font-family: 'Courier New', monospace; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #7f8c8d; }
                        .warning-box { background-color: #fadbd8; padding: 15px; border-radius: 5px; margin: 15px 0; border-left: 4px solid #e74c3c; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✗ Solicitud Rechazada</h1>
                        </div>
                        <div class="content">
                            <p class="rejected">Tu cambio de placa ha sido rechazado</p>
                            <p>Hola %s,</p>
                            <p>Lamentamos informarte que tu solicitud de cambio de placa ha sido <strong>rechazada</strong> por nuestro equipo de Back Office.</p>
                            
                            <div class="plate-box">
                                <p><strong>Placa actual:</strong> <span class="plate">%s</span></p>
                                <p><strong>Placa solicitada:</strong> <span class="plate">%s</span></p>
                            </div>
                            
                            <div class="warning-box">
                                %s
                            </div>
                            
                            <p><strong>¿Qué puedes hacer?</strong></p>
                            <ul>
                                <li>Revisar los documentos y evidencia que presentaste</li>
                                <li>Contactar a soporte para más información</li>
                                <li>Presentar una nueva solicitud con la documentación correcta</li>
                            </ul>
                            
                            <p>Tu suscripción sigue activa con la placa original.</p>
                            <p>Si tienes alguna pregunta, no dudes en contactarnos.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un mensaje automatizado, por favor no responder a este correo.</p>
                            <p>&copy; 2025 %s. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(userName, oldPlate, newPlate, notesSection, appName);
    }
}
