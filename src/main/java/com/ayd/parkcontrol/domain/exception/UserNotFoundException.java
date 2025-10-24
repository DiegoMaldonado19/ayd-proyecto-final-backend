package com.ayd.parkcontrol.domain.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long userId) {
        super("Usuario con ID " + userId + " no encontrado");
    }
}
