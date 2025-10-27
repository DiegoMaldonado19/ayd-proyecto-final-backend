package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.audit.OperationType;
import com.ayd.parkcontrol.infrastructure.persistence.entity.OperationTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class OperationTypeMapper {

    public OperationType toDomain(OperationTypeEntity entity) {
        if (entity == null) {
            return null;
        }

        return OperationType.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
