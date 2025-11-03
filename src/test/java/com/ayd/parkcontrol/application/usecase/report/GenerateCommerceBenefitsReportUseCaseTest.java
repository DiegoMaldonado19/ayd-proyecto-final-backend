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
class GenerateCommerceBenefitsReportUseCaseTest {

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private GenerateCommerceBenefitsReportUseCase generateCommerceBenefitsReportUseCase;

    private SubscriptionEntity activeSubscription;
    private SubscriptionEntity expiredSubscription;

    @BeforeEach
    void setUp() {
        activeSubscription = SubscriptionEntity.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .licensePlate("ABC123")
                .frozenRateBase(BigDecimal.valueOf(100.00))
                .purchaseDate(LocalDateTime.now())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        expiredSubscription = SubscriptionEntity.builder()
                .id(2L)
                .userId(2L)
                .planId(1L)
                .licensePlate("XYZ789")
                .frozenRateBase(BigDecimal.valueOf(50.00))
                .purchaseDate(LocalDateTime.now().minusDays(60))
                .startDate(LocalDateTime.now().minusDays(60))
                .endDate(LocalDateTime.now().minusDays(30))
                .build();
    }

    @Test
    void execute_shouldGenerateCommerceBenefitsReport_whenSubscriptionsExist() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(activeSubscription, expiredSubscription));

        List<Map<String, Object>> result = generateCommerceBenefitsReportUseCase.execute();

        assertThat(result).hasSize(1);
        Map<String, Object> benefits = result.get(0);
        assertThat(benefits.get("total_subscriptions_sold")).isEqualTo(2);
        assertThat(benefits.get("active_subscriptions")).isEqualTo(1L);
        assertThat(benefits.get("total_revenue_from_subscriptions")).isEqualTo(BigDecimal.valueOf(150.00));
    }

    @Test
    void execute_shouldReturnZeroRevenue_whenNoSubscriptionsExist() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = generateCommerceBenefitsReportUseCase.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("total_subscriptions_sold")).isEqualTo(0);
        assertThat(result.get(0).get("total_revenue_from_subscriptions")).isEqualTo(BigDecimal.ZERO);
    }
}
