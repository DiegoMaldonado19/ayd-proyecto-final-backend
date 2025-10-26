package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.PurchaseSubscriptionRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicateLicensePlateException;
import com.ayd.parkcontrol.domain.exception.SubscriptionPlanNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionPlanRepository;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RateBaseHistoryEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRateBaseHistoryRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionStatusTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private JpaRateBaseHistoryRepository rateBaseRepository;

    @Mock
    private JpaSubscriptionStatusTypeRepository statusTypeRepository;

    @Mock
    private SubscriptionDtoMapper mapper;

    @InjectMocks
    private PurchaseSubscriptionUseCase useCase;

    private PurchaseSubscriptionRequest request;
    private SubscriptionPlan subscriptionPlan;
    private RateBaseHistoryEntity rateHistory;
    private SubscriptionStatusTypeEntity statusType;
    private Subscription subscription;
    private SubscriptionResponse response;

    @BeforeEach
    void setUp() {
        request = PurchaseSubscriptionRequest.builder()
                .planId(1L)
                .licensePlate("ABC123")
                .isAnnual(false)
                .autoRenewEnabled(false)
                .build();

        subscriptionPlan = SubscriptionPlan.builder()
                .id(1L)
                .planTypeId(1)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .isActive(true)
                .build();

        rateHistory = RateBaseHistoryEntity.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("10.00"))
                .build();

        statusType = SubscriptionStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .name("Activa")
                .build();

        subscription = Subscription.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .licensePlate("ABC123")
                .frozenRateBase(new BigDecimal("10.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .statusTypeId(1)
                .build();

        response = SubscriptionResponse.builder()
                .id(1L)
                .licensePlate("ABC123")
                .build();
    }

    @Test
    void execute_shouldCreateSubscription_whenValidRequest() {
        when(subscriptionRepository.existsActiveLicensePlate("ABC123")).thenReturn(false);
        when(subscriptionPlanRepository.findById(1L)).thenReturn(Optional.of(subscriptionPlan));
        when(rateBaseRepository.findCurrentRate()).thenReturn(Optional.of(rateHistory));
        when(statusTypeRepository.findByCode("ACTIVE")).thenReturn(Optional.of(statusType));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(mapper.toSubscriptionResponse(subscription)).thenReturn(response);

        SubscriptionResponse result = useCase.execute(request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getLicensePlate()).isEqualTo("ABC123");
        verify(subscriptionRepository).existsActiveLicensePlate("ABC123");
        verify(subscriptionPlanRepository).findById(1L);
        verify(rateBaseRepository).findCurrentRate();
        verify(statusTypeRepository).findByCode("ACTIVE");
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void execute_shouldThrowException_whenPlanNotFound() {
        when(subscriptionRepository.existsActiveLicensePlate("ABC123")).thenReturn(false);
        when(subscriptionPlanRepository.findById(999L)).thenReturn(Optional.empty());
        request.setPlanId(999L);

        assertThatThrownBy(() -> useCase.execute(request, 1L))
                .isInstanceOf(SubscriptionPlanNotFoundException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowException_whenLicensePlateAlreadyActive() {
        when(subscriptionRepository.existsActiveLicensePlate("ABC123")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request, 1L))
                .isInstanceOf(DuplicateLicensePlateException.class)
                .hasMessageContaining("ABC123");

        verify(subscriptionRepository, never()).save(any());
    }
}
