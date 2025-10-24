package com.ayd.parkcontrol.domain.exception;

public class DuplicateBranchNameException extends RuntimeException {
    public DuplicateBranchNameException(String message) {
        super(message);
    }

    public DuplicateBranchNameException() {
        super("Branch name already exists");
    }
}
