package com.ayd.parkcontrol.domain.exception;

public class DuplicatePlanTypeException extends BusinessRuleException {
    public DuplicatePlanTypeException(String message) {
        super(message);
    }
}
