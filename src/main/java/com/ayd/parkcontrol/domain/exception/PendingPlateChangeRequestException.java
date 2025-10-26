package com.ayd.parkcontrol.domain.exception;

public class PendingPlateChangeRequestException extends BusinessRuleException {
    public PendingPlateChangeRequestException(Long subscriptionId) {
        super(String.format("La suscripción %d ya tiene una solicitud de cambio de placa pendiente", subscriptionId));
    }
}
