package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetActiveSubscriptionsCountUseCaseTest {

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private GetActiveSubscriptionsCountUseCase getActiveSubscriptionsCountUseCase;

    private SubscriptionEntity activeSubscription;
    private SubscriptionEntity expiredSubscription;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        activeSubscription = SubscriptionEntity.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .licensePlate("ABC123")
                .frozenRateBase(BigDecimal.valueOf(100.00))
                .purchaseDate(now.minusDays(10))
                .startDate(now.minusDays(10))
                .endDate(now.plusDays(20))
                .build();

        expiredSubscription = SubscriptionEntity.builder()
                .id(2L)
                .userId(2L)
                .planId(1L)
                .licensePlate("XYZ789")
                .frozenRateBase(BigDecimal.valueOf(50.00))
                .purchaseDate(now.minusDays(60))
                .startDate(now.minusDays(60))
                .endDate(now.minusDays(30))
                .build();
    }

    @Test
    void execute_shouldCountActiveSubscriptions_whenSubscriptionsExist() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(activeSubscription, expiredSubscription));

        Long result = getActiveSubscriptionsCountUseCase.execute();

        assertThat(result).isEqualTo(1L);
    }

    @Test
    void execute_shouldReturnZero_whenNoActiveSubscriptionsExist() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(expiredSubscription));

        Long result = getActiveSubscriptionsCountUseCase.execute();

        assertThat(result).isEqualTo(0L);
    }

    @Test
    void execute_shouldReturnZero_whenNoSubscriptionsExist() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        Long result = getActiveSubscriptionsCountUseCase.execute();

        assertThat(result).isEqualTo(0L);
    }
}
