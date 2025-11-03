package com.ayd.parkcontrol.application.usecase.report;

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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateSubscriptionReportUseCaseTest {

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private GenerateSubscriptionReportUseCase generateSubscriptionReportUseCase;

    private SubscriptionEntity activeSubscription;
    private SubscriptionEntity expiredSubscription;
    private SubscriptionEntity expiringSoonSubscription;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        activeSubscription = SubscriptionEntity.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .licensePlate("ABC123")
                .frozenRateBase(BigDecimal.valueOf(100.00))
                .purchaseDate(now)
                .startDate(now)
                .endDate(now.plusDays(60))
                .build();

        expiredSubscription = SubscriptionEntity.builder()
                .id(2L)
                .userId(2L)
                .planId(1L)
                .licensePlate("XYZ789")
                .frozenRateBase(BigDecimal.valueOf(50.00))
                .purchaseDate(now.minusDays(90))
                .startDate(now.minusDays(90))
                .endDate(now.minusDays(30))
                .build();

        expiringSoonSubscription = SubscriptionEntity.builder()
                .id(3L)
                .userId(3L)
                .planId(1L)
                .licensePlate("DEF456")
                .frozenRateBase(BigDecimal.valueOf(75.00))
                .purchaseDate(now.minusDays(30))
                .startDate(now.minusDays(30))
                .endDate(now.plusDays(15))
                .build();
    }

    @Test
    void execute_shouldGenerateSubscriptionReport_whenSubscriptionsExist() {
        when(subscriptionRepository.findAll())
                .thenReturn(List.of(activeSubscription, expiredSubscription, expiringSoonSubscription));

        List<Map<String, Object>> result = generateSubscriptionReportUseCase.execute();

        assertThat(result).hasSize(1);
        Map<String, Object> summary = result.get(0);
        assertThat(summary.get("total_subscriptions")).isEqualTo(3);
        assertThat(summary.get("active_subscriptions")).isEqualTo(2L);
        assertThat(summary.get("expired_subscriptions")).isEqualTo(1L);
        assertThat(summary.get("expiring_soon")).isEqualTo(1L);
    }

    @Test
    void execute_shouldReturnZeroCounts_whenNoSubscriptionsExist() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = generateSubscriptionReportUseCase.execute();

        assertThat(result).hasSize(1);
        Map<String, Object> summary = result.get(0);
        assertThat(summary.get("total_subscriptions")).isEqualTo(0);
        assertThat(summary.get("active_subscriptions")).isEqualTo(0L);
        assertThat(summary.get("expired_subscriptions")).isEqualTo(0L);
        assertThat(summary.get("expiring_soon")).isEqualTo(0L);
    }

    @Test
    void execute_shouldCountOnlyActiveSubscriptions_whenFilteringByEndDate() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(activeSubscription, expiredSubscription));

        List<Map<String, Object>> result = generateSubscriptionReportUseCase.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("active_subscriptions")).isEqualTo(1L);
        assertThat(result.get(0).get("expired_subscriptions")).isEqualTo(1L);
    }
}
