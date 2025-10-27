package com.ayd.parkcontrol.infrastructure.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TimeUtils.
 */
class TimeUtilsTest {

    @Test
    void minutesToHours_shouldConvertCorrectly() {
        // 125 minutos = 2.08 horas (redondeado hacia abajo)
        BigDecimal result = TimeUtils.minutesToHours(125);
        assertEquals(new BigDecimal("2.08"), result);
    }

    @Test
    void minutesToHours_shouldRoundDown() {
        // 127 minutos = 2.116666... -> 2.11 (hacia abajo)
        BigDecimal result = TimeUtils.minutesToHours(127);
        assertEquals(new BigDecimal("2.11"), result);
    }

    @Test
    void minutesToHours_withZero_shouldReturnZero() {
        BigDecimal result = TimeUtils.minutesToHours(0);
        assertEquals(BigDecimal.ZERO.setScale(2), result);
    }

    @Test
    void minutesToHours_withNegative_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            TimeUtils.minutesToHours(-10);
        });
    }

    @Test
    void calculateDurationInHours_shouldCalculateCorrectly() {
        LocalDateTime start = LocalDateTime.of(2025, 10, 26, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 10, 26, 12, 30);

        BigDecimal result = TimeUtils.calculateDurationInHours(start, end);
        assertEquals(new BigDecimal("2.50"), result);
    }

    @Test
    void calculateDurationInHours_withEndBeforeStart_shouldThrowException() {
        LocalDateTime start = LocalDateTime.of(2025, 10, 26, 12, 0);
        LocalDateTime end = LocalDateTime.of(2025, 10, 26, 10, 0);

        assertThrows(IllegalArgumentException.class, () -> {
            TimeUtils.calculateDurationInHours(start, end);
        });
    }

    @Test
    void calculateDurationInMinutes_shouldCalculateCorrectly() {
        LocalDateTime start = LocalDateTime.of(2025, 10, 26, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 10, 26, 12, 30);

        long result = TimeUtils.calculateDurationInMinutes(start, end);
        assertEquals(150, result);
    }

    @Test
    void roundHoursDown_shouldRoundCorrectly() {
        BigDecimal hours = new BigDecimal("2.458");
        BigDecimal result = TimeUtils.roundHoursDown(hours);
        assertEquals(new BigDecimal("2.45"), result);
    }
}
