package com.ayd.parkcontrol.domain.exception;

public class AuditLogNotFoundException extends RuntimeException {

    public AuditLogNotFoundException(String message) {
        super(message);
    }

    public AuditLogNotFoundException(Long auditLogId) {
        super("Audit log con ID " + auditLogId + " no encontrado");
    }
}
