package com.ayd.parkcontrol.infrastructure.scheduler;

import com.ayd.parkcontrol.application.dto.request.subscription.PurchaseSubscriptionRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import com.ayd.parkcontrol.application.usecase.subscription.PurchaseSubscriptionUseCase;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AutoRenewSubscriptionsJob.
 */
@ExtendWith(MockitoExtension.class)
class AutoRenewSubscriptionsJobTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PurchaseSubscriptionUseCase purchaseSubscriptionUseCase;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AutoRenewSubscriptionsJob job;

    private Subscription mockSubscription;
    private SubscriptionPlan mockPlan;

    @BeforeEach
    void setUp() {
        mockPlan = SubscriptionPlan.builder()
                .id(1L)
                .planTypeName("Plan Premium")
                .monthlyHours(50)
                .build();

        mockSubscription = Subscription.builder()
                .id(1L)
                .userId(100L)
                .planId(1L)
                .licensePlate("ABC123")
                .isAnnual(false)
                .userEmail("user@example.com")
                .endDate(LocalDateTime.now().plusDays(5))
                .plan(mockPlan)
                .build();
    }

    @Test
    void processAutoRenewals_withNoExpiring_shouldNotRenew() {
        // Arrange
        when(subscriptionRepository.findExpiringSoonWithAutoRenew(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        job.processAutoRenewals();

        // Assert
        verify(purchaseSubscriptionUseCase, never()).execute(any(), any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void processAutoRenewals_withExpiring_shouldRenewSuccessfully() {
        // Arrange
        when(subscriptionRepository.findExpiringSoonWithAutoRenew(any(), any()))
                .thenReturn(List.of(mockSubscription));

        SubscriptionResponse mockResponse = new SubscriptionResponse();
        when(purchaseSubscriptionUseCase.execute(any(PurchaseSubscriptionRequest.class), eq(100L)))
                .thenReturn(mockResponse);

        // Act
        job.processAutoRenewals();

        // Assert
        ArgumentCaptor<PurchaseSubscriptionRequest> requestCaptor = 
                ArgumentCaptor.forClass(PurchaseSubscriptionRequest.class);
        
        verify(purchaseSubscriptionUseCase).execute(requestCaptor.capture(), eq(100L));
        verify(emailService).sendEmail(eq("user@example.com"), contains("renovada"), any());

        PurchaseSubscriptionRequest captured = requestCaptor.getValue();
        assertEquals(1L, captured.getPlanId());
        assertEquals("ABC123", captured.getLicensePlate());
        assertFalse(captured.getIsAnnual());
        assertTrue(captured.getAutoRenewEnabled());
    }

    @Test
    void processAutoRenewals_withMultipleSubscriptions_shouldProcessAll() {
        // Arrange
        Subscription subscription2 = Subscription.builder()
                .id(2L)
                .userId(200L)
                .planId(1L)
                .licensePlate("XYZ789")
                .isAnnual(true)
                .userEmail("user2@example.com")
                .endDate(LocalDateTime.now().plusDays(6))
                .plan(mockPlan)
                .build();

        when(subscriptionRepository.findExpiringSoonWithAutoRenew(any(), any()))
                .thenReturn(Arrays.asList(mockSubscription, subscription2));

        when(purchaseSubscriptionUseCase.execute(any(), any()))
                .thenReturn(new SubscriptionResponse());

        // Act
        job.processAutoRenewals();

        // Assert
        verify(purchaseSubscriptionUseCase, times(2)).execute(any(), any());
        verify(emailService, times(2)).sendEmail(any(), contains("renovada"), any());
    }

    @Test
    void processAutoRenewals_withRenewalFailure_shouldSendFailureEmail() {
        // Arrange
        when(subscriptionRepository.findExpiringSoonWithAutoRenew(any(), any()))
                .thenReturn(List.of(mockSubscription));

        when(purchaseSubscriptionUseCase.execute(any(), any()))
                .thenThrow(new RuntimeException("Payment failed"));

        // Act
        job.processAutoRenewals();

        // Assert
        verify(emailService).sendEmail(
                eq("user@example.com"), 
                contains("Fallo en renovación"), 
                contains("Payment failed")
        );
    }

    @Test
    void processAutoRenewals_withNullUserEmail_shouldNotSendEmail() {
        // Arrange
        mockSubscription.setUserEmail(null);
        
        when(subscriptionRepository.findExpiringSoonWithAutoRenew(any(), any()))
                .thenReturn(List.of(mockSubscription));

        when(purchaseSubscriptionUseCase.execute(any(), any()))
                .thenReturn(new SubscriptionResponse());

        // Act
        job.processAutoRenewals();

        // Assert
        verify(purchaseSubscriptionUseCase).execute(any(), any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void processAutoRenewals_withEmailServiceFailure_shouldContinueProcessing() {
        // Arrange
        when(subscriptionRepository.findExpiringSoonWithAutoRenew(any(), any()))
                .thenReturn(List.of(mockSubscription));

        when(purchaseSubscriptionUseCase.execute(any(), any()))
                .thenReturn(new SubscriptionResponse());

        doThrow(new RuntimeException("Email service down"))
                .when(emailService).sendEmail(any(), any(), any());

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> job.processAutoRenewals());
        
        verify(purchaseSubscriptionUseCase).execute(any(), any());
    }

    @Test
    void processAutoRenewals_withAnnualSubscription_shouldMaintainAnnualFlag() {
        // Arrange
        mockSubscription.setIsAnnual(true);
        
        when(subscriptionRepository.findExpiringSoonWithAutoRenew(any(), any()))
                .thenReturn(List.of(mockSubscription));

        when(purchaseSubscriptionUseCase.execute(any(), any()))
                .thenReturn(new SubscriptionResponse());

        // Act
        job.processAutoRenewals();

        // Assert
        ArgumentCaptor<PurchaseSubscriptionRequest> captor = 
                ArgumentCaptor.forClass(PurchaseSubscriptionRequest.class);
        
        verify(purchaseSubscriptionUseCase).execute(captor.capture(), any());
        assertTrue(captor.getValue().getIsAnnual());
    }

    @Test
    void processAutoRenewals_shouldQueryCorrectTimeWindow() {
        // Arrange
        when(subscriptionRepository.findExpiringSoonWithAutoRenew(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        job.processAutoRenewals();

        // Assert
        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        
        verify(subscriptionRepository).findExpiringSoonWithAutoRenew(
                startCaptor.capture(), 
                endCaptor.capture()
        );

        // Verificar que la ventana es de 7 días
        LocalDateTime start = startCaptor.getValue();
        LocalDateTime end = endCaptor.getValue();
        
        assertTrue(end.isAfter(start));
        assertTrue(end.isBefore(start.plusDays(8))); // 7 días + margen
    }
}
