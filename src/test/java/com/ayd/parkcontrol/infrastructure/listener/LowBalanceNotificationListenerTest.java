package com.ayd.parkcontrol.infrastructure.listener;

import com.ayd.parkcontrol.application.event.SubscriptionHoursUpdatedEvent;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LowBalanceNotificationListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private LowBalanceNotificationListener listener;

    private SubscriptionHoursUpdatedEvent event;

    @BeforeEach
    void setUp() {
        event = new SubscriptionHoursUpdatedEvent(
                1L, // subscriptionId
                1L, // userId
                "user@test.com", // userEmail
                BigDecimal.valueOf(80.0), // hoursConsumed
                100, // monthlyHours
                BigDecimal.valueOf(80.0) // percentage
        );
    }

    @Test
    void handleSubscriptionHoursUpdated_shouldSendNotificationAt80Percent() {
        // Arrange
        event = new SubscriptionHoursUpdatedEvent(
                1L, 1L, "user@test.com",
                BigDecimal.valueOf(80.0), 100, BigDecimal.valueOf(80.0));

        // Act
        listener.handleSubscriptionHoursUpdated(event);

        // Assert
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(1)).sendEmail(
                eq("user@test.com"),
                subjectCaptor.capture(),
                bodyCaptor.capture());

        assertTrue(subjectCaptor.getValue().contains("80%"));
        assertTrue(bodyCaptor.getValue().contains("80.00%"));
        assertTrue(bodyCaptor.getValue().contains("100 horas"));
    }

    @Test
    void handleSubscriptionHoursUpdated_shouldSendNotificationAt100Percent() {
        // Arrange
        event = new SubscriptionHoursUpdatedEvent(
                2L, 2L, "user2@test.com",
                BigDecimal.valueOf(100.0), 100, BigDecimal.valueOf(100.0));

        // Act
        listener.handleSubscriptionHoursUpdated(event);

        // Assert
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(2)).sendEmail(
                eq("user2@test.com"),
                subjectCaptor.capture(),
                bodyCaptor.capture());

        // Should send both 80% and 100% notifications
        assertTrue(subjectCaptor.getAllValues().stream()
                .anyMatch(s -> s.contains("agotado")));
    }

    @Test
    void handleSubscriptionHoursUpdated_shouldNotSendDuplicateNotificationAt80Percent() {
        // Arrange
        event = new SubscriptionHoursUpdatedEvent(
                3L, 3L, "user3@test.com",
                BigDecimal.valueOf(85.0), 100, BigDecimal.valueOf(85.0));

        // Act
        listener.handleSubscriptionHoursUpdated(event);
        listener.handleSubscriptionHoursUpdated(event); // Second call

        // Assert
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void handleSubscriptionHoursUpdated_shouldNotSendNotificationBelow80Percent() {
        // Arrange
        event = new SubscriptionHoursUpdatedEvent(
                4L, 4L, "user4@test.com",
                BigDecimal.valueOf(70.0), 100, BigDecimal.valueOf(70.0));

        // Act
        listener.handleSubscriptionHoursUpdated(event);

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void handleSubscriptionHoursUpdated_shouldHandleEmailServiceException() {
        // Arrange
        event = new SubscriptionHoursUpdatedEvent(
                5L, 5L, "user5@test.com",
                BigDecimal.valueOf(90.0), 100, BigDecimal.valueOf(90.0));

        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act & Assert
        assertDoesNotThrow(() -> listener.handleSubscriptionHoursUpdated(event));
    }

    @Test
    void resetMonthlyNotifications_shouldClearAllNotifications() {
        // Arrange
        SubscriptionHoursUpdatedEvent event1 = new SubscriptionHoursUpdatedEvent(
                6L, 6L, "user6@test.com",
                BigDecimal.valueOf(80.0), 100, BigDecimal.valueOf(80.0));

        listener.handleSubscriptionHoursUpdated(event1);

        // Act
        listener.resetMonthlyNotifications();

        // Re-send same event after reset
        listener.handleSubscriptionHoursUpdated(event1);

        // Assert
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void handleSubscriptionHoursUpdated_shouldSendBothNotificationsAt100Percent() {
        // Arrange
        event = new SubscriptionHoursUpdatedEvent(
                7L, 7L, "user7@test.com",
                BigDecimal.valueOf(100.0), 100, BigDecimal.valueOf(100.0));

        // Act
        listener.handleSubscriptionHoursUpdated(event);

        // Assert
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(2)).sendEmail(
                eq("user7@test.com"),
                subjectCaptor.capture(),
                anyString());

        assertEquals(2, subjectCaptor.getAllValues().size());
        assertTrue(subjectCaptor.getAllValues().stream()
                .anyMatch(s -> s.contains("80%")));
        assertTrue(subjectCaptor.getAllValues().stream()
                .anyMatch(s -> s.contains("agotado")));
    }
}
