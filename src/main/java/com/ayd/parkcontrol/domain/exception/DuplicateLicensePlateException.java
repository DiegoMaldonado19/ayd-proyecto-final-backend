package com.ayd.parkcontrol.domain.exception;

public class DuplicateLicensePlateException extends BusinessRuleException {
    public DuplicateLicensePlateException(String message) {
        super(message);
    }
}
