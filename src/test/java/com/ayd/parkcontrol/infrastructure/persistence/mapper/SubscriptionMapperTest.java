package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SubscriptionMapperTest {

    private SubscriptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SubscriptionMapper();
    }

    @Test
    void toDomainPlan_shouldReturnNull_whenEntityIsNull() {
        SubscriptionPlan result = mapper.toDomain((SubscriptionPlanEntity) null);

        assertThat(result).isNull();
    }

    @Test
    void toDomainPlan_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        SubscriptionPlanTypeEntity planType = SubscriptionPlanTypeEntity.builder()
                .id(1)
                .name("Full Access")
                .code("FULL_ACCESS")
                .build();

        SubscriptionPlanEntity entity = SubscriptionPlanEntity.builder()
                .id(1L)
                .planTypeId(1)
                .planType(planType)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("12.00"))
                .description("Acceso completo 7 dias a la semana")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        SubscriptionPlan result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPlanTypeId()).isEqualTo(1);
        assertThat(result.getPlanTypeName()).isEqualTo("Full Access");
        assertThat(result.getPlanTypeCode()).isEqualTo("FULL_ACCESS");
        assertThat(result.getMonthlyHours()).isEqualTo(224);
        assertThat(result.getMonthlyDiscountPercentage()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getAnnualAdditionalDiscountPercentage()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(result.getDescription()).isEqualTo("Acceso completo 7 dias a la semana");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void toDomainPlan_shouldHandleNullPlanType() {
        SubscriptionPlanEntity entity = SubscriptionPlanEntity.builder()
                .id(2L)
                .planTypeId(2)
                .planType(null)
                .monthlyHours(160)
                .monthlyDiscountPercentage(new BigDecimal("15.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("6.00"))
                .description("Workweek")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        SubscriptionPlan result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getPlanTypeName()).isNull();
        assertThat(result.getPlanTypeCode()).isNull();
    }

    @Test
    void toEntityPlan_shouldReturnNull_whenDomainIsNull() {
        SubscriptionPlanEntity result = mapper.toEntity((SubscriptionPlan) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntityPlan_shouldMapCorrectly_whenDomainIsValid() {
        LocalDateTime now = LocalDateTime.now();
        SubscriptionPlan domain = SubscriptionPlan.builder()
                .id(3L)
                .planTypeId(3)
                .planTypeName("Office Light")
                .planTypeCode("OFFICE_LIGHT")
                .monthlyHours(80)
                .monthlyDiscountPercentage(new BigDecimal("10.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("5.00"))
                .description("Lunes a viernes, 4 horas diarias")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        SubscriptionPlanEntity result = mapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getPlanTypeId()).isEqualTo(3);
        assertThat(result.getMonthlyHours()).isEqualTo(80);
        assertThat(result.getMonthlyDiscountPercentage()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(result.getAnnualAdditionalDiscountPercentage()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.getDescription()).isEqualTo("Lunes a viernes, 4 horas diarias");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void toDomainSubscription_shouldReturnNull_whenEntityIsNull() {
        Subscription result = mapper.toDomain((SubscriptionEntity) null);

        assertThat(result).isNull();
    }

    @Test
    void toDomainSubscription_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();

        SubscriptionPlanEntity planEntity = SubscriptionPlanEntity.builder()
                .id(1L)
                .planTypeId(1)
                .monthlyHours(224)
                .monthlyDiscountPercentage(new BigDecimal("20.00"))
                .annualAdditionalDiscountPercentage(new BigDecimal("12.00"))
                .description("Full Access")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        SubscriptionStatusTypeEntity statusEntity = SubscriptionStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .name("Activa")
                .build();

        SubscriptionEntity entity = SubscriptionEntity.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .licensePlate("ABC-123")
                .frozenRateBase(new BigDecimal("12.00"))
                .purchaseDate(now)
                .startDate(now)
                .endDate(now.plusMonths(1))
                .consumedHours(new BigDecimal("45.50"))
                .statusTypeId(1)
                .status(statusEntity)
                .isAnnual(false)
                .notified80Percent(false)
                .autoRenewEnabled(true)
                .createdAt(now)
                .updatedAt(now)
                .plan(planEntity)
                .user(userEntity)
                .build();

        Subscription result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getPlanId()).isEqualTo(1L);
        assertThat(result.getLicensePlate()).isEqualTo("ABC-123");
        assertThat(result.getFrozenRateBase()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(result.getConsumedHours()).isEqualByComparingTo(new BigDecimal("45.50"));
        assertThat(result.getStatusTypeId()).isEqualTo(1);
        assertThat(result.getStatusName()).isEqualTo("Activa");
        assertThat(result.getIsAnnual()).isFalse();
        assertThat(result.getNotified80Percent()).isFalse();
        assertThat(result.getAutoRenewEnabled()).isTrue();
        assertThat(result.getPlan()).isNotNull();
        assertThat(result.getUserEmail()).isEqualTo("test@parkcontrol.com");
        assertThat(result.getUserName()).isEqualTo("John Doe");
    }

    @Test
    void toDomainSubscription_shouldHandleNullRelations() {
        LocalDateTime now = LocalDateTime.now();

        SubscriptionEntity entity = SubscriptionEntity.builder()
                .id(2L)
                .userId(2L)
                .planId(2L)
                .licensePlate("DEF-456")
                .frozenRateBase(new BigDecimal("12.00"))
                .purchaseDate(now)
                .startDate(now)
                .endDate(now.plusYears(1))
                .consumedHours(new BigDecimal("100.00"))
                .statusTypeId(1)
                .status(null)
                .isAnnual(true)
                .notified80Percent(true)
                .autoRenewEnabled(false)
                .createdAt(now)
                .updatedAt(now)
                .plan(null)
                .user(null)
                .build();

        Subscription result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getStatusName()).isNull();
        assertThat(result.getPlan()).isNull();
        assertThat(result.getUserEmail()).isNull();
        assertThat(result.getUserName()).isNull();
    }

    @Test
    void toEntitySubscription_shouldReturnNull_whenDomainIsNull() {
        SubscriptionEntity result = mapper.toEntity((Subscription) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntitySubscription_shouldMapCorrectly_whenDomainIsValid() {
        LocalDateTime now = LocalDateTime.now();

        Subscription domain = Subscription.builder()
                .id(3L)
                .userId(3L)
                .planId(3L)
                .licensePlate("GHI-789")
                .frozenRateBase(new BigDecimal("12.00"))
                .purchaseDate(now)
                .startDate(now)
                .endDate(now.plusMonths(1))
                .consumedHours(new BigDecimal("15.25"))
                .statusTypeId(1)
                .isAnnual(false)
                .notified80Percent(false)
                .autoRenewEnabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        SubscriptionEntity result = mapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getUserId()).isEqualTo(3L);
        assertThat(result.getPlanId()).isEqualTo(3L);
        assertThat(result.getLicensePlate()).isEqualTo("GHI-789");
        assertThat(result.getFrozenRateBase()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(result.getConsumedHours()).isEqualByComparingTo(new BigDecimal("15.25"));
        assertThat(result.getStatusTypeId()).isEqualTo(1);
        assertThat(result.getIsAnnual()).isFalse();
        assertThat(result.getNotified80Percent()).isFalse();
        assertThat(result.getAutoRenewEnabled()).isTrue();
    }

    @Test
    void bidirectionalMappingPlan_shouldPreserveData() {
        LocalDateTime now = LocalDateTime.now();
        SubscriptionPlan originalPlan = SubscriptionPlan.builder()
                .id(4L)
                .planTypeId(4)
                .monthlyHours(112)
                .monthlyDiscountPercentage(new BigDecimal("7.50"))
                .annualAdditionalDiscountPercentage(new BigDecimal("4.00"))
                .description("Diario Flexible")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        SubscriptionPlanEntity entity = mapper.toEntity(originalPlan);
        SubscriptionPlan resultPlan = mapper.toDomain(entity);

        assertThat(resultPlan).isNotNull();
        assertThat(resultPlan.getId()).isEqualTo(originalPlan.getId());
        assertThat(resultPlan.getPlanTypeId()).isEqualTo(originalPlan.getPlanTypeId());
        assertThat(resultPlan.getMonthlyHours()).isEqualTo(originalPlan.getMonthlyHours());
        assertThat(resultPlan.getMonthlyDiscountPercentage())
                .isEqualByComparingTo(originalPlan.getMonthlyDiscountPercentage());
    }

    @Test
    void bidirectionalMappingSubscription_shouldPreserveData() {
        LocalDateTime now = LocalDateTime.now();
        Subscription originalSubscription = Subscription.builder()
                .id(5L)
                .userId(5L)
                .planId(5L)
                .licensePlate("JKL-012")
                .frozenRateBase(new BigDecimal("12.00"))
                .purchaseDate(now)
                .startDate(now)
                .endDate(now.plusMonths(1))
                .consumedHours(new BigDecimal("32.00"))
                .statusTypeId(1)
                .isAnnual(false)
                .notified80Percent(false)
                .autoRenewEnabled(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        SubscriptionEntity entity = mapper.toEntity(originalSubscription);
        Subscription resultSubscription = mapper.toDomain(entity);

        assertThat(resultSubscription).isNotNull();
        assertThat(resultSubscription.getId()).isEqualTo(originalSubscription.getId());
        assertThat(resultSubscription.getUserId()).isEqualTo(originalSubscription.getUserId());
        assertThat(resultSubscription.getLicensePlate()).isEqualTo(originalSubscription.getLicensePlate());
        assertThat(resultSubscription.getConsumedHours()).isEqualByComparingTo(originalSubscription.getConsumedHours());
    }
}
