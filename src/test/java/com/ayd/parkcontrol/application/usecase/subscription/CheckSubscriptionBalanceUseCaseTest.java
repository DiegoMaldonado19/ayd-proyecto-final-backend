package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionBalanceResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckSubscriptionBalanceUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionDtoMapper mapper;

    @InjectMocks
    private CheckSubscriptionBalanceUseCase useCase;

    private Subscription subscription;
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionBalanceResponse balanceResponse;

    @BeforeEach
    void setUp() {
        subscriptionPlan = SubscriptionPlan.builder()
                .id(1L)
                .planTypeId(1)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .build();

        subscription = Subscription.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .licensePlate("ABC123")
                .frozenRateBase(new BigDecimal("10.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .consumedHours(new BigDecimal("50"))
                .statusTypeId(1)
                .plan(subscriptionPlan)
                .build();

        balanceResponse = SubscriptionBalanceResponse.builder()
                .subscriptionId(1L)
                .monthlyHours(224)
                .consumedHours(new BigDecimal("50"))
                .remainingHours(new BigDecimal("174"))
                .consumptionPercentage(new BigDecimal("22.32"))
                .build();
    }

    @Test
    void execute_shouldReturnBalance_whenSubscriptionExists() {
        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(subscription));
        when(mapper.toBalanceResponse(subscription)).thenReturn(balanceResponse);

        SubscriptionBalanceResponse result = useCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getMonthlyHours()).isEqualTo(224);
        assertThat(result.getConsumedHours()).isEqualByComparingTo(new BigDecimal("50"));
        assertThat(result.getRemainingHours()).isEqualByComparingTo(new BigDecimal("174"));

        verify(subscriptionRepository).findByIdWithDetails(1L);
        verify(mapper).toBalanceResponse(subscription);
    }

    @Test
    void execute_shouldCalculateCorrectPercentage_whenFullyConsumed() {
        subscription = Subscription.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .licensePlate("ABC123")
                .frozenRateBase(new BigDecimal("10.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .consumedHours(new BigDecimal("224"))
                .statusTypeId(1)
                .plan(subscriptionPlan)
                .build();

        SubscriptionBalanceResponse fullBalanceResponse = SubscriptionBalanceResponse.builder()
                .subscriptionId(1L)
                .monthlyHours(224)
                .consumedHours(new BigDecimal("224"))
                .remainingHours(BigDecimal.ZERO)
                .consumptionPercentage(new BigDecimal("100.00"))
                .build();

        when(subscriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(subscription));
        when(mapper.toBalanceResponse(subscription)).thenReturn(fullBalanceResponse);

        SubscriptionBalanceResponse result = useCase.execute(1L);

        assertThat(result.getRemainingHours()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getConsumptionPercentage()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void execute_shouldThrowException_whenSubscriptionNotFound() {
        when(subscriptionRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessageContaining("not found with ID: 999");

        verify(subscriptionRepository).findByIdWithDetails(999L);
    }
}
