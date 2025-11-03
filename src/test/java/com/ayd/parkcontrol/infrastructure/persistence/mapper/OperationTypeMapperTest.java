package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.audit.OperationType;
import com.ayd.parkcontrol.infrastructure.persistence.entity.OperationTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OperationTypeMapperTest {

    private OperationTypeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OperationTypeMapper();
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        OperationType result = mapper.toDomain(null);

        assertThat(result).isNull();
    }

    @Test
    void toDomain_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        OperationTypeEntity entity = OperationTypeEntity.builder()
                .id(1)
                .code("INSERT")
                .name("Insercion")
                .createdAt(now)
                .build();

        OperationType result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCode()).isEqualTo("INSERT");
        assertThat(result.getName()).isEqualTo("Insercion");
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldMapInsertOperation() {
        LocalDateTime now = LocalDateTime.now();
        OperationTypeEntity entity = OperationTypeEntity.builder()
                .id(1)
                .code("INSERT")
                .name("Insercion")
                .createdAt(now)
                .build();

        OperationType result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("INSERT");
        assertThat(result.getName()).isEqualTo("Insercion");
    }

    @Test
    void toDomain_shouldMapUpdateOperation() {
        LocalDateTime now = LocalDateTime.now();
        OperationTypeEntity entity = OperationTypeEntity.builder()
                .id(2)
                .code("UPDATE")
                .name("Actualizacion")
                .createdAt(now)
                .build();

        OperationType result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("UPDATE");
        assertThat(result.getName()).isEqualTo("Actualizacion");
    }

    @Test
    void toDomain_shouldMapDeleteOperation() {
        LocalDateTime now = LocalDateTime.now();
        OperationTypeEntity entity = OperationTypeEntity.builder()
                .id(3)
                .code("DELETE")
                .name("Eliminacion")
                .createdAt(now)
                .build();

        OperationType result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("DELETE");
        assertThat(result.getName()).isEqualTo("Eliminacion");
    }

    @Test
    void toDomain_shouldMapLoginOperation() {
        LocalDateTime now = LocalDateTime.now();
        OperationTypeEntity entity = OperationTypeEntity.builder()
                .id(4)
                .code("LOGIN")
                .name("Inicio de Sesion")
                .createdAt(now)
                .build();

        OperationType result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("LOGIN");
        assertThat(result.getName()).isEqualTo("Inicio de Sesion");
    }

    @Test
    void toDomain_shouldMapLogoutOperation() {
        LocalDateTime now = LocalDateTime.now();
        OperationTypeEntity entity = OperationTypeEntity.builder()
                .id(5)
                .code("LOGOUT")
                .name("Cierre de Sesion")
                .createdAt(now)
                .build();

        OperationType result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("LOGOUT");
        assertThat(result.getName()).isEqualTo("Cierre de Sesion");
    }

    @Test
    void toDomain_shouldMapAllOperationTypes() {
        LocalDateTime now = LocalDateTime.now();

        OperationTypeEntity insertEntity = OperationTypeEntity.builder()
                .id(1)
                .code("INSERT")
                .name("Insercion")
                .createdAt(now)
                .build();

        OperationTypeEntity updateEntity = OperationTypeEntity.builder()
                .id(2)
                .code("UPDATE")
                .name("Actualizacion")
                .createdAt(now)
                .build();

        OperationTypeEntity deleteEntity = OperationTypeEntity.builder()
                .id(3)
                .code("DELETE")
                .name("Eliminacion")
                .createdAt(now)
                .build();

        OperationTypeEntity loginEntity = OperationTypeEntity.builder()
                .id(4)
                .code("LOGIN")
                .name("Inicio de Sesion")
                .createdAt(now)
                .build();

        OperationTypeEntity logoutEntity = OperationTypeEntity.builder()
                .id(5)
                .code("LOGOUT")
                .name("Cierre de Sesion")
                .createdAt(now)
                .build();

        OperationType insertResult = mapper.toDomain(insertEntity);
        OperationType updateResult = mapper.toDomain(updateEntity);
        OperationType deleteResult = mapper.toDomain(deleteEntity);
        OperationType loginResult = mapper.toDomain(loginEntity);
        OperationType logoutResult = mapper.toDomain(logoutEntity);

        assertThat(insertResult.getId()).isEqualTo(1);
        assertThat(updateResult.getId()).isEqualTo(2);
        assertThat(deleteResult.getId()).isEqualTo(3);
        assertThat(loginResult.getId()).isEqualTo(4);
        assertThat(logoutResult.getId()).isEqualTo(5);

        assertThat(insertResult.getCode()).isEqualTo("INSERT");
        assertThat(updateResult.getCode()).isEqualTo("UPDATE");
        assertThat(deleteResult.getCode()).isEqualTo("DELETE");
        assertThat(loginResult.getCode()).isEqualTo("LOGIN");
        assertThat(logoutResult.getCode()).isEqualTo("LOGOUT");
    }

    @Test
    void toDomain_shouldPreserveCreatedAt() {
        LocalDateTime specificTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        OperationTypeEntity entity = OperationTypeEntity.builder()
                .id(1)
                .code("INSERT")
                .name("Insercion")
                .createdAt(specificTime)
                .build();

        OperationType result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getCreatedAt()).isEqualTo(specificTime);
    }

    @Test
    void toDomain_shouldHandleDifferentIds() {
        LocalDateTime now = LocalDateTime.now();

        OperationTypeEntity entity1 = OperationTypeEntity.builder()
                .id(1)
                .code("INSERT")
                .name("Insercion")
                .createdAt(now)
                .build();

        OperationTypeEntity entity2 = OperationTypeEntity.builder()
                .id(100)
                .code("CUSTOM")
                .name("Custom Operation")
                .createdAt(now)
                .build();

        OperationType result1 = mapper.toDomain(entity1);
        OperationType result2 = mapper.toDomain(entity2);

        assertThat(result1.getId()).isEqualTo(1);
        assertThat(result2.getId()).isEqualTo(100);
    }

    @Test
    void toDomain_shouldHandleMultipleMappingsIndependently() {
        LocalDateTime now = LocalDateTime.now();

        OperationTypeEntity entity1 = OperationTypeEntity.builder()
                .id(1)
                .code("INSERT")
                .name("Insercion")
                .createdAt(now)
                .build();

        OperationTypeEntity entity2 = OperationTypeEntity.builder()
                .id(2)
                .code("UPDATE")
                .name("Actualizacion")
                .createdAt(now.plusHours(1))
                .build();

        OperationType result1 = mapper.toDomain(entity1);
        OperationType result2 = mapper.toDomain(entity2);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
        assertThat(result1.getCode()).isNotEqualTo(result2.getCode());
        assertThat(result1.getCreatedAt()).isNotEqualTo(result2.getCreatedAt());
    }
}
