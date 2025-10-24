package com.ayd.parkcontrol.domain.exception;

public class DuplicateEmailException extends BusinessRuleException {

    public DuplicateEmailException(String email) {
        super("El email '" + email + "' ya está registrado en el sistema");
    }
}
