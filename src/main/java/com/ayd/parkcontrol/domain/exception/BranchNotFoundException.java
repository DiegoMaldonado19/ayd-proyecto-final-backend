package com.ayd.parkcontrol.domain.exception;

public class BranchNotFoundException extends RuntimeException {
    public BranchNotFoundException(String message) {
        super(message);
    }

    public BranchNotFoundException() {
        super("Branch not found");
    }
}
