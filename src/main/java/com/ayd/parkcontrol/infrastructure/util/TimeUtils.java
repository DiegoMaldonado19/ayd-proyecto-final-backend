package com.ayd.parkcontrol.infrastructure.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Utilidad para cálculos de tiempo con redondeo consistente.
 * 
 * Regla de Negocio: Todos los cálculos de tiempo deben usar RoundingMode.DOWN
 * para evitar cobros excesivos al cliente.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
public final class TimeUtils {

    private TimeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Convierte minutos a horas con redondeo hacia abajo.
     * 
     * Ejemplo: 125 minutos = 2.08 horas (redondeado a 2 decimales)
     * 
     * @param minutes cantidad de minutos
     * @return horas con 2 decimales, redondeadas hacia abajo
     */
    public static BigDecimal minutesToHours(long minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Minutes cannot be negative");
        }
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.DOWN);
    }

    /**
     * Calcula la duración en horas entre dos fechas con redondeo hacia abajo.
     * 
     * @param start fecha/hora de inicio
     * @param end   fecha/hora de fin
     * @return horas transcurridas con 2 decimales, redondeadas hacia abajo
     * @throws IllegalArgumentException si end es anterior a start
     */
    public static BigDecimal calculateDurationInHours(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        long minutes = ChronoUnit.MINUTES.between(start, end);
        return minutesToHours(minutes);
    }

    /**
     * Calcula la duración en minutos entre dos fechas.
     * 
     * @param start fecha/hora de inicio
     * @param end   fecha/hora de fin
     * @return minutos transcurridos
     * @throws IllegalArgumentException si end es anterior a start
     */
    public static long calculateDurationInMinutes(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Convierte Duration a horas con redondeo hacia abajo.
     * 
     * @param duration duración
     * @return horas con 2 decimales, redondeadas hacia abajo
     */
    public static BigDecimal durationToHours(Duration duration) {
        long minutes = duration.toMinutes();
        return minutesToHours(minutes);
    }

    /**
     * Redondea horas hacia abajo a 2 decimales.
     * 
     * @param hours horas a redondear
     * @return horas redondeadas hacia abajo
     */
    public static BigDecimal roundHoursDown(BigDecimal hours) {
        return hours.setScale(2, RoundingMode.DOWN);
    }
}
