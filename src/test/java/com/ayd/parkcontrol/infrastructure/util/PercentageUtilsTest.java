package com.ayd.parkcontrol.infrastructure.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PercentageUtils.
 */
class PercentageUtilsTest {

    @Test
    void calculatePercentage_shouldCalculateCorrectly() {
        // 30 de 120 = 25%
        BigDecimal result = PercentageUtils.calculatePercentage(
                new BigDecimal("30"),
                new BigDecimal("120"));
        assertEquals(new BigDecimal("25.00"), result);
    }

    @Test
    void calculatePercentage_withLongValues_shouldWork() {
        BigDecimal result = PercentageUtils.calculatePercentage(80, 100);
        assertEquals(new BigDecimal("80.00"), result);
    }

    @Test
    void calculatePercentage_withZeroTotal_shouldReturnZero() {
        BigDecimal result = PercentageUtils.calculatePercentage(
                new BigDecimal("10"),
                BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculateAmountFromPercentage_shouldCalculateCorrectly() {
        // 20% de 150 = 30
        BigDecimal result = PercentageUtils.calculateAmountFromPercentage(
                new BigDecimal("150"),
                new BigDecimal("20"));
        assertEquals(new BigDecimal("30.00"), result);
    }

    @Test
    void increaseByPercentage_shouldIncreaseCorrectly() {
        // 100 + 15% = 115
        BigDecimal result = PercentageUtils.increaseByPercentage(
                new BigDecimal("100"),
                new BigDecimal("15"));
        assertEquals(new BigDecimal("115.00"), result);
    }

    @Test
    void decreaseByPercentage_shouldDecreaseCorrectly() {
        // 100 - 15% = 85
        BigDecimal result = PercentageUtils.decreaseByPercentage(
                new BigDecimal("100"),
                new BigDecimal("15"));
        assertEquals(new BigDecimal("85.00"), result);
    }

    @Test
    void validatePercentageRange_withValidValue_shouldNotThrow() {
        assertDoesNotThrow(() -> {
            PercentageUtils.validatePercentageRange(new BigDecimal("50"));
        });

        assertDoesNotThrow(() -> {
            PercentageUtils.validatePercentageRange(BigDecimal.ZERO);
        });

        assertDoesNotThrow(() -> {
            PercentageUtils.validatePercentageRange(new BigDecimal("100"));
        });
    }

    @Test
    void validatePercentageRange_withInvalidValue_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            PercentageUtils.validatePercentageRange(new BigDecimal("150"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            PercentageUtils.validatePercentageRange(new BigDecimal("-10"));
        });
    }

    @Test
    void roundPercentage_shouldRoundCorrectly() {
        BigDecimal result = PercentageUtils.roundPercentage(new BigDecimal("33.456"));
        assertEquals(new BigDecimal("33.46"), result);
    }
}
