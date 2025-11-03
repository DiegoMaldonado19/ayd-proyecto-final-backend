package com.ayd.parkcontrol.domain.model.audit;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para el modelo de dominio AuditLog.
 */
class AuditLogTest {

    @Test
    void builder_shouldCreateAuditLogCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .userId(100L)
                .module("USER")
                .entity("User")
                .operationTypeId(1)
                .description("User created")
                .previousValues(null)
                .newValues("{\"id\":1,\"email\":\"user@example.com\"}")
                .clientIp("192.168.1.1")
                .createdAt(now)
                .build();

        assertThat(auditLog).isNotNull();
        assertThat(auditLog.getId()).isEqualTo(1L);
        assertThat(auditLog.getUserId()).isEqualTo(100L);
        assertThat(auditLog.getModule()).isEqualTo("USER");
        assertThat(auditLog.getEntity()).isEqualTo("User");
        assertThat(auditLog.getOperationTypeId()).isEqualTo(1);
        assertThat(auditLog.getDescription()).isEqualTo("User created");
        assertThat(auditLog.getPreviousValues()).isNull();
        assertThat(auditLog.getNewValues()).isEqualTo("{\"id\":1,\"email\":\"user@example.com\"}");
        assertThat(auditLog.getClientIp()).isEqualTo("192.168.1.1");
        assertThat(auditLog.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        AuditLog auditLog = new AuditLog();
        assertThat(auditLog).isNotNull();
        assertThat(auditLog.getId()).isNull();
        assertThat(auditLog.getUserId()).isNull();
        assertThat(auditLog.getModule()).isNull();
        assertThat(auditLog.getEntity()).isNull();
        assertThat(auditLog.getOperationTypeId()).isNull();
        assertThat(auditLog.getDescription()).isNull();
        assertThat(auditLog.getPreviousValues()).isNull();
        assertThat(auditLog.getNewValues()).isNull();
        assertThat(auditLog.getClientIp()).isNull();
        assertThat(auditLog.getCreatedAt()).isNull();
    }

    @Test
    void allArgsConstructor_shouldCreateObjectWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        AuditLog auditLog = new AuditLog(
                2L,
                200L,
                "BRANCH",
                "Branch",
                2,
                "Branch updated",
                "{\"name\":\"Old Branch\"}",
                "{\"name\":\"New Branch\"}",
                "10.0.0.1",
                now);

        assertThat(auditLog.getId()).isEqualTo(2L);
        assertThat(auditLog.getUserId()).isEqualTo(200L);
        assertThat(auditLog.getModule()).isEqualTo("BRANCH");
        assertThat(auditLog.getEntity()).isEqualTo("Branch");
        assertThat(auditLog.getOperationTypeId()).isEqualTo(2);
        assertThat(auditLog.getDescription()).isEqualTo("Branch updated");
        assertThat(auditLog.getPreviousValues()).isEqualTo("{\"name\":\"Old Branch\"}");
        assertThat(auditLog.getNewValues()).isEqualTo("{\"name\":\"New Branch\"}");
        assertThat(auditLog.getClientIp()).isEqualTo("10.0.0.1");
        assertThat(auditLog.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        AuditLog auditLog = new AuditLog();
        LocalDateTime now = LocalDateTime.now();

        auditLog.setId(3L);
        auditLog.setUserId(300L);
        auditLog.setModule("SUBSCRIPTION");
        auditLog.setEntity("Subscription");
        auditLog.setOperationTypeId(3);
        auditLog.setDescription("Subscription deleted");
        auditLog.setPreviousValues("{\"status\":\"active\"}");
        auditLog.setNewValues("{\"status\":\"deleted\"}");
        auditLog.setClientIp("172.16.0.1");
        auditLog.setCreatedAt(now);

        assertThat(auditLog.getId()).isEqualTo(3L);
        assertThat(auditLog.getUserId()).isEqualTo(300L);
        assertThat(auditLog.getModule()).isEqualTo("SUBSCRIPTION");
        assertThat(auditLog.getEntity()).isEqualTo("Subscription");
        assertThat(auditLog.getOperationTypeId()).isEqualTo(3);
        assertThat(auditLog.getDescription()).isEqualTo("Subscription deleted");
        assertThat(auditLog.getPreviousValues()).isEqualTo("{\"status\":\"active\"}");
        assertThat(auditLog.getNewValues()).isEqualTo("{\"status\":\"deleted\"}");
        assertThat(auditLog.getClientIp()).isEqualTo("172.16.0.1");
        assertThat(auditLog.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void equals_shouldReturnTrue_forSameValues() {
        LocalDateTime now = LocalDateTime.now();
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .userId(100L)
                .module("USER")
                .entity("User")
                .operationTypeId(1)
                .description("User created")
                .createdAt(now)
                .build();

        AuditLog log2 = AuditLog.builder()
                .id(1L)
                .userId(100L)
                .module("USER")
                .entity("User")
                .operationTypeId(1)
                .description("User created")
                .createdAt(now)
                .build();

        assertThat(log1).isEqualTo(log2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .userId(100L)
                .module("USER")
                .build();

        AuditLog log2 = AuditLog.builder()
                .id(1L)
                .userId(100L)
                .module("USER")
                .build();

        assertThat(log1.hashCode()).isEqualTo(log2.hashCode());
    }

    @Test
    void toString_shouldContainKeyFields() {
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .userId(100L)
                .module("USER")
                .entity("User")
                .operationTypeId(1)
                .description("User created")
                .clientIp("192.168.1.1")
                .build();

        String toString = auditLog.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("userId=100");
        assertThat(toString).contains("module=USER");
        assertThat(toString).contains("entity=User");
        assertThat(toString).contains("operationTypeId=1");
        assertThat(toString).contains("description=User created");
        assertThat(toString).contains("clientIp=192.168.1.1");
    }
}
