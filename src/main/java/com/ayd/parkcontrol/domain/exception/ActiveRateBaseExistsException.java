package com.ayd.parkcontrol.domain.exception;

public class ActiveRateBaseExistsException extends BusinessRuleException {
    
    public ActiveRateBaseExistsException() {
        super("Una tarifa base ya se encuentra activa. Solo puede existir una tarifa base activa en el sistema.");
    }
    
    public ActiveRateBaseExistsException(String message) {
        super(message);
    }
}