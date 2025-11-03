package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.audit.AuditLogResponse;
import com.ayd.parkcontrol.domain.model.audit.AuditLog;
import com.ayd.parkcontrol.domain.model.audit.OperationType;
import com.ayd.parkcontrol.domain.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogDtoMapperTest {

    private AuditLogDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AuditLogDtoMapper();
    }

    @Test
    void toResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .userId(100L)
                .module("TICKETS")
                .entity("Ticket")
                .operationTypeId(1)
                .description("Ticket creado")
                .previousValues("{}")
                .newValues("{\"id\":1,\"status\":\"ACTIVE\"}")
                .clientIp("192.168.1.1")
                .createdAt(now)
                .build();

        User user = User.builder()
                .id(100L)
                .email("admin@example.com")
                .build();

        OperationType operationType = OperationType.builder()
                .id(1)
                .code("CREATE")
                .name("Crear")
                .build();

        AuditLogResponse result = mapper.toResponse(auditLog, user, operationType);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getUserEmail()).isEqualTo("admin@example.com");
        assertThat(result.getModule()).isEqualTo("TICKETS");
        assertThat(result.getEntity()).isEqualTo("Ticket");
        assertThat(result.getOperationType()).isEqualTo("Crear");
        assertThat(result.getDescription()).isEqualTo("Ticket creado");
        assertThat(result.getPreviousValues()).isEqualTo("{}");
        assertThat(result.getNewValues()).isEqualTo("{\"id\":1,\"status\":\"ACTIVE\"}");
        assertThat(result.getClientIp()).isEqualTo("192.168.1.1");
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_withNullUser_shouldMapWithoutUserEmail() {
        LocalDateTime now = LocalDateTime.now();

        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .userId(100L)
                .module("TICKETS")
                .entity("Ticket")
                .operationTypeId(1)
                .description("Ticket creado")
                .clientIp("192.168.1.1")
                .createdAt(now)
                .build();

        OperationType operationType = OperationType.builder()
                .id(1)
                .name("Crear")
                .build();

        AuditLogResponse result = mapper.toResponse(auditLog, null, operationType);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getUserEmail()).isNull();
    }

    @Test
    void toResponse_withNullOperationType_shouldMapWithoutOperationTypeName() {
        LocalDateTime now = LocalDateTime.now();

        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .userId(100L)
                .module("TICKETS")
                .entity("Ticket")
                .operationTypeId(1)
                .createdAt(now)
                .build();

        User user = User.builder()
                .id(100L)
                .email("admin@example.com")
                .build();

        AuditLogResponse result = mapper.toResponse(auditLog, user, null);

        assertThat(result).isNotNull();
        assertThat(result.getOperationType()).isNull();
    }
}
