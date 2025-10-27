package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.audit.AuditLogResponse;
import com.ayd.parkcontrol.domain.model.audit.AuditLog;
import com.ayd.parkcontrol.domain.model.audit.OperationType;
import com.ayd.parkcontrol.domain.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class AuditLogDtoMapper {

    public AuditLogResponse toResponse(AuditLog auditLog, User user, OperationType operationType) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .userEmail(user != null ? user.getEmail() : null)
                .module(auditLog.getModule())
                .entity(auditLog.getEntity())
                .operationType(operationType != null ? operationType.getName() : null)
                .description(auditLog.getDescription())
                .previousValues(auditLog.getPreviousValues())
                .newValues(auditLog.getNewValues())
                .clientIp(auditLog.getClientIp())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
