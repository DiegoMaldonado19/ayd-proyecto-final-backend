package com.ayd.parkcontrol.domain.model.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private Long id;
    private Long userId;
    private String module;
    private String entity;
    private Integer operationTypeId;
    private String description;
    private String previousValues;
    private String newValues;
    private String clientIp;
    private LocalDateTime createdAt;
}
