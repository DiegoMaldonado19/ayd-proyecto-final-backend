package com.ayd.parkcontrol.domain.exception;

public class VehicleNotFoundException extends BusinessRuleException {
    public VehicleNotFoundException(String message) {
        super(message);
    }
}
