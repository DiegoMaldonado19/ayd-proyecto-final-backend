package com.ayd.parkcontrol.domain.exception;

public class VehicleTypeNotFoundException extends BusinessRuleException {
    public VehicleTypeNotFoundException(String message) {
        super(message);
    }
}
