package com.ayd.parkcontrol.domain.exception;

public class SubscriptionPlanNotFoundException extends BusinessRuleException {
    public SubscriptionPlanNotFoundException(String message) {
        super(message);
    }
}
