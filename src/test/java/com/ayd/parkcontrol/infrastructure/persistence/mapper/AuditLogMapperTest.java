package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.audit.AuditLog;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AuditLogEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AuditLogMapperTest {

    private AuditLogMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AuditLogMapper();
    }

    @Test
    void toEntity_shouldReturnNull_whenAuditLogIsNull() {
        AuditLogEntity result = mapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldMapCorrectly_whenAuditLogIsValid() {
        LocalDateTime now = LocalDateTime.now();
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .userId(1L)
                .module("USER")
                .entity("users")
                .operationTypeId(1)
                .description("User created")
                .previousValues(null)
                .newValues("{\"email\":\"test@parkcontrol.com\"}")
                .clientIp("192.168.1.1")
                .createdAt(now)
                .build();

        AuditLogEntity result = mapper.toEntity(auditLog);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getModule()).isEqualTo("USER");
        assertThat(result.getEntity()).isEqualTo("users");
        assertThat(result.getOperationTypeId()).isEqualTo(1);
        assertThat(result.getDescription()).isEqualTo("User created");
        assertThat(result.getPreviousValues()).isNull();
        assertThat(result.getNewValues()).isEqualTo("{\"email\":\"test@parkcontrol.com\"}");
        assertThat(result.getClientIp()).isEqualTo("192.168.1.1");
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        AuditLog result = mapper.toDomain(null);

        assertThat(result).isNull();
    }

    @Test
    void toDomain_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        AuditLogEntity entity = AuditLogEntity.builder()
                .id(2L)
                .userId(2L)
                .module("SUBSCRIPTION")
                .entity("subscriptions")
                .operationTypeId(2)
                .description("Subscription updated")
                .previousValues("{\"status\":\"ACTIVE\"}")
                .newValues("{\"status\":\"EXPIRED\"}")
                .clientIp("10.0.0.1")
                .createdAt(now)
                .build();

        AuditLog result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getModule()).isEqualTo("SUBSCRIPTION");
        assertThat(result.getEntity()).isEqualTo("subscriptions");
        assertThat(result.getOperationTypeId()).isEqualTo(2);
        assertThat(result.getDescription()).isEqualTo("Subscription updated");
        assertThat(result.getPreviousValues()).isEqualTo("{\"status\":\"ACTIVE\"}");
        assertThat(result.getNewValues()).isEqualTo("{\"status\":\"EXPIRED\"}");
        assertThat(result.getClientIp()).isEqualTo("10.0.0.1");
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldHandleNullValues() {
        LocalDateTime now = LocalDateTime.now();
        AuditLog auditLog = AuditLog.builder()
                .id(3L)
                .userId(3L)
                .module("TICKET")
                .entity("tickets")
                .operationTypeId(3)
                .description("Ticket deleted")
                .previousValues("{\"folio\":\"TICKET-001\"}")
                .newValues(null)
                .clientIp(null)
                .createdAt(now)
                .build();

        AuditLogEntity result = mapper.toEntity(auditLog);

        assertThat(result).isNotNull();
        assertThat(result.getPreviousValues()).isEqualTo("{\"folio\":\"TICKET-001\"}");
        assertThat(result.getNewValues()).isNull();
        assertThat(result.getClientIp()).isNull();
    }

    @Test
    void toDomain_shouldHandleNullValues() {
        LocalDateTime now = LocalDateTime.now();
        AuditLogEntity entity = AuditLogEntity.builder()
                .id(4L)
                .userId(4L)
                .module("AUTH")
                .entity("users")
                .operationTypeId(4)
                .description("User login")
                .previousValues(null)
                .newValues(null)
                .clientIp("172.16.0.1")
                .createdAt(now)
                .build();

        AuditLog result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getPreviousValues()).isNull();
        assertThat(result.getNewValues()).isNull();
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        LocalDateTime now = LocalDateTime.now();
        AuditLog originalAuditLog = AuditLog.builder()
                .id(5L)
                .userId(5L)
                .module("BRANCH")
                .entity("branches")
                .operationTypeId(2)
                .description("Branch capacity updated")
                .previousValues("{\"capacity_4r\":100}")
                .newValues("{\"capacity_4r\":120}")
                .clientIp("192.168.10.5")
                .createdAt(now)
                .build();

        AuditLogEntity entity = mapper.toEntity(originalAuditLog);
        AuditLog resultAuditLog = mapper.toDomain(entity);

        assertThat(resultAuditLog).isNotNull();
        assertThat(resultAuditLog.getId()).isEqualTo(originalAuditLog.getId());
        assertThat(resultAuditLog.getUserId()).isEqualTo(originalAuditLog.getUserId());
        assertThat(resultAuditLog.getModule()).isEqualTo(originalAuditLog.getModule());
        assertThat(resultAuditLog.getEntity()).isEqualTo(originalAuditLog.getEntity());
        assertThat(resultAuditLog.getOperationTypeId()).isEqualTo(originalAuditLog.getOperationTypeId());
        assertThat(resultAuditLog.getDescription()).isEqualTo(originalAuditLog.getDescription());
        assertThat(resultAuditLog.getPreviousValues()).isEqualTo(originalAuditLog.getPreviousValues());
        assertThat(resultAuditLog.getNewValues()).isEqualTo(originalAuditLog.getNewValues());
        assertThat(resultAuditLog.getClientIp()).isEqualTo(originalAuditLog.getClientIp());
    }

    @Test
    void toEntity_shouldHandleAllOperationTypes() {
        LocalDateTime now = LocalDateTime.now();

        AuditLog insertLog = AuditLog.builder()
                .id(1L)
                .userId(1L)
                .module("USER")
                .entity("users")
                .operationTypeId(1)
                .description("INSERT operation")
                .previousValues(null)
                .newValues("{}")
                .clientIp("192.168.1.1")
                .createdAt(now)
                .build();

        AuditLog updateLog = AuditLog.builder()
                .id(2L)
                .userId(1L)
                .module("USER")
                .entity("users")
                .operationTypeId(2)
                .description("UPDATE operation")
                .previousValues("{}")
                .newValues("{}")
                .clientIp("192.168.1.1")
                .createdAt(now)
                .build();

        AuditLog deleteLog = AuditLog.builder()
                .id(3L)
                .userId(1L)
                .module("USER")
                .entity("users")
                .operationTypeId(3)
                .description("DELETE operation")
                .previousValues("{}")
                .newValues(null)
                .clientIp("192.168.1.1")
                .createdAt(now)
                .build();

        AuditLogEntity insertEntity = mapper.toEntity(insertLog);
        AuditLogEntity updateEntity = mapper.toEntity(updateLog);
        AuditLogEntity deleteEntity = mapper.toEntity(deleteLog);

        assertThat(insertEntity.getOperationTypeId()).isEqualTo(1);
        assertThat(updateEntity.getOperationTypeId()).isEqualTo(2);
        assertThat(deleteEntity.getOperationTypeId()).isEqualTo(3);
    }

    @Test
    void toDomain_shouldHandleDifferentModules() {
        LocalDateTime now = LocalDateTime.now();

        AuditLogEntity userEntity = AuditLogEntity.builder()
                .id(1L)
                .userId(1L)
                .module("USER")
                .entity("users")
                .operationTypeId(1)
                .description("User module action")
                .previousValues(null)
                .newValues("{}")
                .clientIp("192.168.1.1")
                .createdAt(now)
                .build();

        AuditLogEntity ticketEntity = AuditLogEntity.builder()
                .id(2L)
                .userId(1L)
                .module("TICKET")
                .entity("tickets")
                .operationTypeId(1)
                .description("Ticket module action")
                .previousValues(null)
                .newValues("{}")
                .clientIp("192.168.1.1")
                .createdAt(now)
                .build();

        AuditLog userLog = mapper.toDomain(userEntity);
        AuditLog ticketLog = mapper.toDomain(ticketEntity);

        assertThat(userLog.getModule()).isEqualTo("USER");
        assertThat(ticketLog.getModule()).isEqualTo("TICKET");
    }
}
