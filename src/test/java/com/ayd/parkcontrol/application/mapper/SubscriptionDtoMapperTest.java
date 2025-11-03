package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionBalanceResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionDtoMapperTest {

    private SubscriptionDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SubscriptionDtoMapper();
    }

    @Test
    void toSubscriptionPlanResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        SubscriptionPlan domain = SubscriptionPlan.builder()
                .id(1L)
                .planTypeId(1)
                .planTypeName("Plan Básico")
                .planTypeCode("BASIC")
                .monthlyHours(20)
                .monthlyDiscountPercentage(new BigDecimal("10.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("5.00"))
                .description("Plan mensual básico")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        SubscriptionPlanResponse result = mapper.toSubscriptionPlanResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPlanTypeId()).isEqualTo(1);
        assertThat(result.getPlanTypeName()).isEqualTo("Plan Básico");
        assertThat(result.getPlanTypeCode()).isEqualTo("BASIC");
        assertThat(result.getMonthlyHours()).isEqualTo(20);
        assertThat(result.getMonthlyDiscountPercentage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getAnnualAdditionalDiscountPercentage()).isEqualTo(new BigDecimal("5.00"));
        assertThat(result.getDescription()).isEqualTo("Plan mensual básico");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toSubscriptionPlanResponse_withNullInput_shouldReturnNull() {
        SubscriptionPlanResponse result = mapper.toSubscriptionPlanResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toSubscriptionResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .id(1L)
                .monthlyHours(20)
                .build();

        Subscription domain = Subscription.builder()
                .id(1L)
                .userId(100L)
                .userEmail("user@example.com")
                .userName("Juan Pérez")
                .plan(plan)
                .licensePlate("P-123ABC")
                .frozenRateBase(new BigDecimal("15.00"))
                .purchaseDate(now.minusDays(5))
                .startDate(now.minusDays(3))
                .endDate(now.plusDays(27))
                .consumedHours(new BigDecimal("5.50"))
                .statusName("Activa")
                .isAnnual(false)
                .autoRenewEnabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        SubscriptionResponse result = mapper.toSubscriptionResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getUserEmail()).isEqualTo("user@example.com");
        assertThat(result.getUserName()).isEqualTo("Juan Pérez");
        assertThat(result.getPlan()).isNotNull();
        assertThat(result.getLicensePlate()).isEqualTo("P-123ABC");
        assertThat(result.getFrozenRateBase()).isEqualTo(new BigDecimal("15.00"));
        assertThat(result.getPurchaseDate()).isEqualTo(now.minusDays(5));
        assertThat(result.getStartDate()).isEqualTo(now.minusDays(3));
        assertThat(result.getEndDate()).isEqualTo(now.plusDays(27));
        assertThat(result.getConsumedHours()).isEqualTo(new BigDecimal("5.50"));
        assertThat(result.getStatusName()).isEqualTo("Activa");
        assertThat(result.getIsAnnual()).isFalse();
        assertThat(result.getAutoRenewEnabled()).isTrue();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toSubscriptionResponse_withNullInput_shouldReturnNull() {
        SubscriptionResponse result = mapper.toSubscriptionResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toBalanceResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(15);

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .id(1L)
                .monthlyHours(20)
                .build();

        Subscription subscription = Subscription.builder()
                .id(1L)
                .plan(plan)
                .consumedHours(new BigDecimal("5.00"))
                .endDate(endDate)
                .build();

        SubscriptionBalanceResponse result = mapper.toBalanceResponse(subscription);

        assertThat(result).isNotNull();
        assertThat(result.getSubscriptionId()).isEqualTo(1L);
        assertThat(result.getMonthlyHours()).isEqualTo(20);
        assertThat(result.getConsumedHours()).isEqualTo(new BigDecimal("5.00"));
        assertThat(result.getRemainingHours()).isEqualTo(new BigDecimal("15.00"));
        assertThat(result.getConsumptionPercentage()).isEqualTo(new BigDecimal("25.00"));
        assertThat(result.getDaysUntilExpiration()).isGreaterThanOrEqualTo(14L);
    }

    @Test
    void toBalanceResponse_withNullConsumedHours_shouldDefaultToZero() {
        LocalDateTime endDate = LocalDateTime.now().plusDays(10);

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .id(1L)
                .monthlyHours(20)
                .build();

        Subscription subscription = Subscription.builder()
                .id(1L)
                .plan(plan)
                .consumedHours(null)
                .endDate(endDate)
                .build();

        SubscriptionBalanceResponse result = mapper.toBalanceResponse(subscription);

        assertThat(result).isNotNull();
        assertThat(result.getConsumedHours()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getRemainingHours()).isEqualTo(new BigDecimal("20"));
        assertThat(result.getConsumptionPercentage()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void toBalanceResponse_withNullSubscription_shouldReturnNull() {
        SubscriptionBalanceResponse result = mapper.toBalanceResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toBalanceResponse_withNullPlan_shouldReturnNull() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .plan(null)
                .build();

        SubscriptionBalanceResponse result = mapper.toBalanceResponse(subscription);
        assertThat(result).isNull();
    }
}
