package com.ayd.parkcontrol.domain.model.rate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para el modelo de dominio RateBase.
 */
class RateBaseTest {

    private RateBase rateBase;

    @BeforeEach
    void setUp() {
        rateBase = RateBase.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("10.00"))
                .startDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2024, 12, 31, 23, 59))
                .isActive(true)
                .createdBy(100L)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();
    }

    @Test
    void builder_shouldCreateRateBaseCorrectly() {
        assertThat(rateBase).isNotNull();
        assertThat(rateBase.getId()).isEqualTo(1L);
        assertThat(rateBase.getAmountPerHour()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(rateBase.getStartDate()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
        assertThat(rateBase.getEndDate()).isEqualTo(LocalDateTime.of(2024, 12, 31, 23, 59));
        assertThat(rateBase.getIsActive()).isTrue();
        assertThat(rateBase.getCreatedBy()).isEqualTo(100L);
        assertThat(rateBase.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        RateBase emptyRate = new RateBase();
        assertThat(emptyRate).isNotNull();
        assertThat(emptyRate.getId()).isNull();
        assertThat(emptyRate.getAmountPerHour()).isNull();
    }

    @Test
    void allArgsConstructor_shouldCreateObjectWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        RateBase rate = new RateBase(
                1L,
                new BigDecimal("15.50"),
                now.minusDays(10),
                now.plusDays(10),
                true,
                100L,
                now);

        assertThat(rate.getId()).isEqualTo(1L);
        assertThat(rate.getAmountPerHour()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(rate.getIsActive()).isTrue();
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        RateBase rate = new RateBase();
        LocalDateTime now = LocalDateTime.now();

        rate.setId(2L);
        rate.setAmountPerHour(new BigDecimal("20.00"));
        rate.setStartDate(now);
        rate.setEndDate(now.plusDays(30));
        rate.setIsActive(false);
        rate.setCreatedBy(200L);
        rate.setCreatedAt(now);

        assertThat(rate.getId()).isEqualTo(2L);
        assertThat(rate.getAmountPerHour()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(rate.getStartDate()).isEqualTo(now);
        assertThat(rate.getEndDate()).isEqualTo(now.plusDays(30));
        assertThat(rate.getIsActive()).isFalse();
        assertThat(rate.getCreatedBy()).isEqualTo(200L);
        assertThat(rate.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void isCurrentlyActive_shouldReturnTrue_whenActiveAndWithinDateRange() {
        LocalDateTime now = LocalDateTime.now();
        RateBase activeRate = RateBase.builder()
                .isActive(true)
                .startDate(now.minusDays(5))
                .endDate(now.plusDays(5))
                .build();

        assertThat(activeRate.isCurrentlyActive()).isTrue();
    }

    @Test
    void isCurrentlyActive_shouldReturnTrue_whenActiveAndNoEndDate() {
        LocalDateTime now = LocalDateTime.now();
        RateBase activeRate = RateBase.builder()
                .isActive(true)
                .startDate(now.minusDays(5))
                .endDate(null)
                .build();

        assertThat(activeRate.isCurrentlyActive()).isTrue();
    }

    @Test
    void isCurrentlyActive_shouldReturnFalse_whenNotActive() {
        LocalDateTime now = LocalDateTime.now();
        RateBase inactiveRate = RateBase.builder()
                .isActive(false)
                .startDate(now.minusDays(5))
                .endDate(now.plusDays(5))
                .build();

        assertThat(inactiveRate.isCurrentlyActive()).isFalse();
    }

    @Test
    void isCurrentlyActive_shouldReturnFalse_whenIsActiveIsNull() {
        LocalDateTime now = LocalDateTime.now();
        RateBase nullActiveRate = RateBase.builder()
                .isActive(null)
                .startDate(now.minusDays(5))
                .endDate(now.plusDays(5))
                .build();

        assertThat(nullActiveRate.isCurrentlyActive()).isFalse();
    }

    @Test
    void isCurrentlyActive_shouldReturnFalse_whenStartDateIsInFuture() {
        LocalDateTime now = LocalDateTime.now();
        RateBase futureRate = RateBase.builder()
                .isActive(true)
                .startDate(now.plusDays(5))
                .endDate(now.plusDays(10))
                .build();

        assertThat(futureRate.isCurrentlyActive()).isFalse();
    }

    @Test
    void isCurrentlyActive_shouldReturnFalse_whenEndDateIsInPast() {
        LocalDateTime now = LocalDateTime.now();
        RateBase expiredRate = RateBase.builder()
                .isActive(true)
                .startDate(now.minusDays(10))
                .endDate(now.minusDays(5))
                .build();

        assertThat(expiredRate.isCurrentlyActive()).isFalse();
    }

    @Test
    void isCurrentlyActive_shouldReturnTrue_whenStartDateIsNull() {
        LocalDateTime now = LocalDateTime.now();
        RateBase noStartDateRate = RateBase.builder()
                .isActive(true)
                .startDate(null)
                .endDate(now.plusDays(5))
                .build();

        assertThat(noStartDateRate.isCurrentlyActive()).isTrue();
    }

    @Test
    void equals_shouldReturnTrue_forSameValues() {
        RateBase rate1 = RateBase.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("10.00"))
                .isActive(true)
                .build();

        RateBase rate2 = RateBase.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("10.00"))
                .isActive(true)
                .build();

        assertThat(rate1).isEqualTo(rate2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        RateBase rate1 = RateBase.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("10.00"))
                .build();

        RateBase rate2 = RateBase.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("10.00"))
                .build();

        assertThat(rate1.hashCode()).isEqualTo(rate2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        String toString = rateBase.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("amountPerHour=10.00");
        assertThat(toString).contains("isActive=true");
    }
}
