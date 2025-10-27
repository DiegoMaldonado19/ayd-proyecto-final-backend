package com.ayd.parkcontrol.infrastructure.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para MoneyUtils.
 */
class MoneyUtilsTest {

    @Test
    void roundAmount_shouldRoundHalfUp() {
        // 45.675 -> 45.68 (HALF_UP)
        BigDecimal result = MoneyUtils.roundAmount(new BigDecimal("45.675"));
        assertEquals(new BigDecimal("45.68"), result);

        // 45.674 -> 45.67 (HALF_UP)
        result = MoneyUtils.roundAmount(new BigDecimal("45.674"));
        assertEquals(new BigDecimal("45.67"), result);
    }

    @Test
    void roundAmount_withNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            MoneyUtils.roundAmount(null);
        });
    }

    @Test
    void calculateCost_shouldMultiplyAndRound() {
        BigDecimal hours = new BigDecimal("2.5");
        BigDecimal rate = new BigDecimal("15.50");

        BigDecimal result = MoneyUtils.calculateCost(hours, rate);
        assertEquals(new BigDecimal("38.75"), result);
    }

    @Test
    void calculateCost_withNegativeHours_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            MoneyUtils.calculateCost(new BigDecimal("-1"), BigDecimal.TEN);
        });
    }

    @Test
    void applyDiscount_shouldCalculateCorrectly() {
        // $100 con 15% descuento = $85.00
        BigDecimal result = MoneyUtils.applyDiscount(
                new BigDecimal("100"),
                new BigDecimal("15"));
        assertEquals(new BigDecimal("85.00"), result);
    }

    @Test
    void applyDiscount_with50Percent_shouldReturnHalf() {
        BigDecimal result = MoneyUtils.applyDiscount(
                new BigDecimal("200"),
                new BigDecimal("50"));
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void applyDiscount_withInvalidPercentage_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            MoneyUtils.applyDiscount(BigDecimal.TEN, new BigDecimal("150"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            MoneyUtils.applyDiscount(BigDecimal.TEN, new BigDecimal("-10"));
        });
    }

    @Test
    void sum_shouldAddAndRound() {
        BigDecimal result = MoneyUtils.sum(
                new BigDecimal("10.555"),
                new BigDecimal("20.444"),
                new BigDecimal("5.001"));
        assertEquals(new BigDecimal("36.00"), result);
    }

    @Test
    void sum_withEmpty_shouldReturnZero() {
        BigDecimal result = MoneyUtils.sum();
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void multiply_shouldMultiplyAndRound() {
        BigDecimal result = MoneyUtils.multiply(
                new BigDecimal("10.50"),
                new BigDecimal("3"));
        assertEquals(new BigDecimal("31.50"), result);
    }

    @Test
    void validatePositiveAmount_withPositive_shouldNotThrow() {
        assertDoesNotThrow(() -> {
            MoneyUtils.validatePositiveAmount(new BigDecimal("10.50"));
        });
    }

    @Test
    void validatePositiveAmount_withZero_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            MoneyUtils.validatePositiveAmount(BigDecimal.ZERO);
        });
    }

    @Test
    void validatePositiveAmount_withNegative_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            MoneyUtils.validatePositiveAmount(new BigDecimal("-5"));
        });
    }
}
