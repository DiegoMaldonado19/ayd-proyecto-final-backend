package com.ayd.parkcontrol.domain.exception;

public class SubscriptionNotFoundException extends BusinessRuleException {
    public SubscriptionNotFoundException(String message) {
        super(message);
    }
}
