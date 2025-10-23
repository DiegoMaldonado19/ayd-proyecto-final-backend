package com.ayd.parkcontrol.application.port.notification;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    void send2FACode(String to, String code);

    void sendPasswordResetEmail(String to, String resetLink);

    void sendPasswordChangedNotification(String to, String userName);
}
