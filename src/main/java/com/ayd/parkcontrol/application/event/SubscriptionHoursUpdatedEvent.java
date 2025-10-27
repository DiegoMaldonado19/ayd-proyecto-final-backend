package com.ayd.parkcontrol.application.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Evento de dominio publicado cuando se actualizan las horas consumidas de una
 * suscripción.
 * 
 * Utilizado para notificar a los usuarios cuando alcanzan el 80% o 100% de sus
 * horas mensuales.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public class SubscriptionHoursUpdatedEvent {

    private final Long subscriptionId;
    private final Long userId;
    private final String userEmail;
    private final BigDecimal hoursConsumed;
    private final Integer monthlyHours;
    private final BigDecimal percentage;
}
