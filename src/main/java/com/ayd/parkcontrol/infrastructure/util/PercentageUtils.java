package com.ayd.parkcontrol.infrastructure.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidad para cálculos de porcentajes con redondeo consistente.
 * 
 * Regla de Negocio: Los porcentajes se calculan con 2 decimales usando HALF_UP.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
public final class PercentageUtils {

    private static final int PERCENTAGE_SCALE = 2;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private PercentageUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Calcula el porcentaje que representa 'part' del 'total'.
     * 
     * Ejemplo: calculatePercentage(30, 120) = 25.00%
     * 
     * @param part  parte del total
     * @param total total
     * @return porcentaje con 2 decimales
     */
    public static BigDecimal calculatePercentage(BigDecimal part, BigDecimal total) {
        if (part == null || total == null) {
            throw new IllegalArgumentException("Part and total cannot be null");
        }

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return part.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el porcentaje que representa 'part' del 'total' (versión con long).
     * 
     * @param part  parte del total
     * @param total total
     * @return porcentaje con 2 decimales
     */
    public static BigDecimal calculatePercentage(long part, long total) {
        return calculatePercentage(BigDecimal.valueOf(part), BigDecimal.valueOf(total));
    }

    /**
     * Calcula qué cantidad representa un porcentaje de un total.
     * 
     * Ejemplo: calculateAmountFromPercentage(150, 20) = 30.00
     * 
     * @param total      total
     * @param percentage porcentaje (0-100)
     * @return cantidad que representa el porcentaje
     */
    public static BigDecimal calculateAmountFromPercentage(BigDecimal total, BigDecimal percentage) {
        if (total == null || percentage == null) {
            throw new IllegalArgumentException("Total and percentage cannot be null");
        }

        validatePercentageRange(percentage);

        return total.multiply(percentage)
                .divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP)
                .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Incrementa un valor en un porcentaje.
     * 
     * Ejemplo: increaseByPercentage(100, 15) = 115.00
     * 
     * @param amount     monto original
     * @param percentage porcentaje de incremento
     * @return monto incrementado
     */
    public static BigDecimal increaseByPercentage(BigDecimal amount, BigDecimal percentage) {
        if (amount == null || percentage == null) {
            throw new IllegalArgumentException("Amount and percentage cannot be null");
        }

        validatePercentageRange(percentage);

        BigDecimal increase = calculateAmountFromPercentage(amount, percentage);
        return amount.add(increase);
    }

    /**
     * Decrementa un valor en un porcentaje.
     * 
     * Ejemplo: decreaseByPercentage(100, 15) = 85.00
     * 
     * @param amount     monto original
     * @param percentage porcentaje de decremento
     * @return monto decrementado
     */
    public static BigDecimal decreaseByPercentage(BigDecimal amount, BigDecimal percentage) {
        if (amount == null || percentage == null) {
            throw new IllegalArgumentException("Amount and percentage cannot be null");
        }

        validatePercentageRange(percentage);

        BigDecimal decrease = calculateAmountFromPercentage(amount, percentage);
        return amount.subtract(decrease);
    }

    /**
     * Valida que un porcentaje esté en el rango 0-100.
     * 
     * @param percentage porcentaje a validar
     * @throws IllegalArgumentException si el porcentaje está fuera de rango
     */
    public static void validatePercentageRange(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.ZERO) < 0 ||
                percentage.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
    }

    /**
     * Redondea un porcentaje a 2 decimales.
     * 
     * @param percentage porcentaje a redondear
     * @return porcentaje redondeado
     */
    public static BigDecimal roundPercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("Percentage cannot be null");
        }
        return percentage.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }
}
