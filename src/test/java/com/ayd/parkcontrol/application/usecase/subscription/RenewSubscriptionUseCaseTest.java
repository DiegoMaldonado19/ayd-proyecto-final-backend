package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.RenewSubscriptionRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RenewSubscriptionUseCase Tests")
class RenewSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionDtoMapper subscriptionMapper;

    @InjectMocks
    private RenewSubscriptionUseCase renewSubscriptionUseCase;

    private Subscription activeSubscription;
    private SubscriptionPlan plan;
    private RenewSubscriptionRequest request;
    private SubscriptionResponse response;

    @BeforeEach
    void setUp() {
        plan = SubscriptionPlan.builder()
                .id(1L)
                .planTypeId(1)
                .planTypeName("Full Access")
                .planTypeCode("FULL_ACCESS")
                .monthlyHours(200)
                .monthlyDiscountPercentage(BigDecimal.valueOf(10.00))
                .annualAdditionalDiscountPercentage(BigDecimal.valueOf(5.00))
                .build();

        activeSubscription = Subscription.builder()
                .id(1L)
                .userId(100L)
                .planId(1L)
                .plan(plan)
                .licensePlate("ABC-123")
                .frozenRateBase(BigDecimal.valueOf(15.00))
                .purchaseDate(LocalDateTime.now().minusMonths(1))
                .startDate(LocalDateTime.now().minusMonths(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .consumedHours(BigDecimal.valueOf(150.00))
                .statusTypeId(1)
                .statusName("Activa")
                .isAnnual(false)
                .autoRenewEnabled(false)
                .build();

        request = RenewSubscriptionRequest.builder()
                .isAnnual(true)
                .autoRenewEnabled(true)
                .build();

        response = SubscriptionResponse.builder()
                .id(1L)
                .userId(100L)
                .licensePlate("ABC-123")
                .statusName("Activa")
                .build();
    }

    @Test
    @DisplayName("Should renew subscription successfully")
    void shouldRenewSubscriptionSuccessfully() {
        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(subscriptionMapper.toSubscriptionResponse(any(Subscription.class))).thenReturn(response);

        SubscriptionResponse result = renewSubscriptionUseCase.execute(1L, request);

        assertNotNull(result);
        verify(subscriptionRepository).findByIdWithDetails(1L);
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(subscriptionMapper).toSubscriptionResponse(any(Subscription.class));
    }

    @Test
    @DisplayName("Should throw exception when subscription not found")
    void shouldThrowExceptionWhenSubscriptionNotFound() {
        when(subscriptionRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> renewSubscriptionUseCase.execute(999L, request));

        verify(subscriptionRepository).findByIdWithDetails(999L);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should throw exception when subscription is not active")
    void shouldThrowExceptionWhenSubscriptionIsNotActive() {
        activeSubscription.setStatusTypeId(3);
        activeSubscription.setStatusName("Cancelada");

        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(activeSubscription));

        assertThrows(BusinessRuleException.class, () -> renewSubscriptionUseCase.execute(1L, request));

        verify(subscriptionRepository).findByIdWithDetails(1L);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should throw exception when subscription is expired")
    void shouldThrowExceptionWhenSubscriptionIsExpired() {
        activeSubscription.setStatusTypeId(2);
        activeSubscription.setStatusName("Vencida");

        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(activeSubscription));

        assertThrows(BusinessRuleException.class, () -> renewSubscriptionUseCase.execute(1L, request));

        verify(subscriptionRepository).findByIdWithDetails(1L);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should renew with monthly period when isAnnual is false")
    void shouldRenewWithMonthlyPeriodWhenIsAnnualIsFalse() {
        request.setIsAnnual(false);

        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription saved = invocation.getArgument(0);
            assertFalse(saved.getIsAnnual());
            return saved;
        });
        when(subscriptionMapper.toSubscriptionResponse(any(Subscription.class))).thenReturn(response);

        SubscriptionResponse result = renewSubscriptionUseCase.execute(1L, request);

        assertNotNull(result);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should reset consumed hours to zero on renewal")
    void shouldResetConsumedHoursToZeroOnRenewal() {
        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription saved = invocation.getArgument(0);
            assertEquals(BigDecimal.ZERO, saved.getConsumedHours());
            return saved;
        });
        when(subscriptionMapper.toSubscriptionResponse(any(Subscription.class))).thenReturn(response);

        renewSubscriptionUseCase.execute(1L, request);

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should update auto renew setting")
    void shouldUpdateAutoRenewSetting() {
        request.setAutoRenewEnabled(true);

        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription saved = invocation.getArgument(0);
            assertTrue(saved.getAutoRenewEnabled());
            return saved;
        });
        when(subscriptionMapper.toSubscriptionResponse(any(Subscription.class))).thenReturn(response);

        renewSubscriptionUseCase.execute(1L, request);

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should extend end date correctly for monthly renewal")
    void shouldExtendEndDateCorrectlyForMonthlyRenewal() {
        request.setIsAnnual(false);
        LocalDateTime originalEndDate = activeSubscription.getEndDate();

        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription saved = invocation.getArgument(0);
            assertTrue(saved.getEndDate().isAfter(originalEndDate));
            return saved;
        });
        when(subscriptionMapper.toSubscriptionResponse(any(Subscription.class))).thenReturn(response);

        renewSubscriptionUseCase.execute(1L, request);

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should extend end date correctly for annual renewal")
    void shouldExtendEndDateCorrectlyForAnnualRenewal() {
        request.setIsAnnual(true);
        LocalDateTime originalEndDate = activeSubscription.getEndDate();

        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription saved = invocation.getArgument(0);
            assertTrue(saved.getEndDate().isAfter(originalEndDate));
            return saved;
        });
        when(subscriptionMapper.toSubscriptionResponse(any(Subscription.class))).thenReturn(response);

        renewSubscriptionUseCase.execute(1L, request);

        verify(subscriptionRepository).save(any(Subscription.class));
    }
}
