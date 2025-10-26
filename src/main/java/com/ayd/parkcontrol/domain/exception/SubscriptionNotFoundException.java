package com.ayd.parkcontrol.domain.exception;

public class SubscriptionNotFoundException extends BusinessRuleException {
    public SubscriptionNotFoundException(String message) {
        super(message);
    }

    public SubscriptionNotFoundException(Long id) {
        super(String.format("Suscripción no encontrada con ID: %d", id));
    }
}
