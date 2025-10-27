package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.audit.AuditLog;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogEntity toEntity(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        return AuditLogEntity.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .module(auditLog.getModule())
                .entity(auditLog.getEntity())
                .operationTypeId(auditLog.getOperationTypeId())
                .description(auditLog.getDescription())
                .previousValues(auditLog.getPreviousValues())
                .newValues(auditLog.getNewValues())
                .clientIp(auditLog.getClientIp())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    public AuditLog toDomain(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }

        return AuditLog.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .module(entity.getModule())
                .entity(entity.getEntity())
                .operationTypeId(entity.getOperationTypeId())
                .description(entity.getDescription())
                .previousValues(entity.getPreviousValues())
                .newValues(entity.getNewValues())
                .clientIp(entity.getClientIp())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
