package com.ayd.parkcontrol.infrastructure.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidad para cálculos monetarios con redondeo consistente.
 * 
 * Regla de Negocio: Todos los cálculos monetarios deben usar
 * RoundingMode.HALF_UP
 * para redondear a 2 decimales (centavos).
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
public final class MoneyUtils {

    private static final int MONEY_SCALE = 2;

    private MoneyUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Redondea un monto a 2 decimales usando HALF_UP.
     * 
     * Ejemplo: 45.678 -> 45.68, 45.674 -> 45.67
     * 
     * @param amount monto a redondear
     * @return monto redondeado a 2 decimales
     */
    public static BigDecimal roundAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el costo total: horas × tarifa por hora.
     * 
     * @param hours       cantidad de horas
     * @param ratePerHour tarifa por hora
     * @return costo total redondeado a 2 decimales
     */
    public static BigDecimal calculateCost(BigDecimal hours, BigDecimal ratePerHour) {
        if (hours == null || ratePerHour == null) {
            throw new IllegalArgumentException("Hours and rate cannot be null");
        }
        if (hours.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Hours cannot be negative");
        }
        if (ratePerHour.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Rate cannot be negative");
        }

        BigDecimal rawCost = hours.multiply(ratePerHour);
        return roundAmount(rawCost);
    }

    /**
     * Aplica un descuento porcentual a un monto.
     * 
     * Ejemplo: $100 con 15% descuento = $85.00
     * 
     * @param amount             monto original
     * @param discountPercentage porcentaje de descuento (0-100)
     * @return monto con descuento aplicado, redondeado a 2 decimales
     */
    public static BigDecimal applyDiscount(BigDecimal amount, BigDecimal discountPercentage) {
        if (amount == null || discountPercentage == null) {
            throw new IllegalArgumentException("Amount and discount percentage cannot be null");
        }
        if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 ||
                discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }

        // Calcular factor de descuento: 1 - (porcentaje / 100)
        BigDecimal discountFactor = BigDecimal.ONE.subtract(
                discountPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

        BigDecimal discountedAmount = amount.multiply(discountFactor);
        return roundAmount(discountedAmount);
    }

    /**
     * Suma múltiples montos y redondea el resultado.
     * 
     * @param amounts montos a sumar
     * @return suma total redondeada a 2 decimales
     */
    public static BigDecimal sum(BigDecimal... amounts) {
        if (amounts == null || amounts.length == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal amount : amounts) {
            if (amount != null) {
                total = total.add(amount);
            }
        }
        return roundAmount(total);
    }

    /**
     * Multiplica un monto por un factor y redondea.
     * 
     * @param amount monto base
     * @param factor factor multiplicador
     * @return resultado redondeado a 2 decimales
     */
    public static BigDecimal multiply(BigDecimal amount, BigDecimal factor) {
        if (amount == null || factor == null) {
            throw new IllegalArgumentException("Amount and factor cannot be null");
        }
        return roundAmount(amount.multiply(factor));
    }

    /**
     * Valida que un monto sea positivo.
     * 
     * @param amount monto a validar
     * @throws IllegalArgumentException si el monto es negativo o cero
     */
    public static void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}
