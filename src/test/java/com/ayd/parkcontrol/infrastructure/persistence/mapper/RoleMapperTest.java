package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RoleMapperTest {

    private RoleMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RoleMapper();
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        Role result = mapper.toDomain(null);

        assertThat(result).isNull();
    }

    @Test
    void toDomain_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        RoleEntity entity = RoleEntity.builder()
                .id(1)
                .name("Administrador")
                .description("Administrador del sistema con acceso total")
                .createdAt(now)
                .build();

        Role result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Administrador");
        assertThat(result.getDescription()).isEqualTo("Administrador del sistema con acceso total");
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldReturnNull_whenRoleIsNull() {
        RoleEntity result = mapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldMapCorrectly_whenRoleIsValid() {
        LocalDateTime now = LocalDateTime.now();
        Role role = Role.builder()
                .id(2)
                .name("Operador Sucursal")
                .description("Operador de sucursal para control de entradas/salidas")
                .createdAt(now)
                .build();

        RoleEntity result = mapper.toEntity(role);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getName()).isEqualTo("Operador Sucursal");
        assertThat(result.getDescription()).isEqualTo("Operador de sucursal para control de entradas/salidas");
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldHandleNullDescription() {
        LocalDateTime now = LocalDateTime.now();
        RoleEntity entity = RoleEntity.builder()
                .id(3)
                .name("Cliente")
                .description(null)
                .createdAt(now)
                .build();

        Role result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("Cliente");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldHandleNullDescription() {
        LocalDateTime now = LocalDateTime.now();
        Role role = Role.builder()
                .id(4)
                .name("Operador Back Office")
                .description(null)
                .createdAt(now)
                .build();

        RoleEntity result = mapper.toEntity(role);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(4);
        assertThat(result.getName()).isEqualTo("Operador Back Office");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        LocalDateTime now = LocalDateTime.now();
        Role originalRole = Role.builder()
                .id(5)
                .name("Administrador Flotilla")
                .description("Administrador de flota empresarial")
                .createdAt(now)
                .build();

        RoleEntity entity = mapper.toEntity(originalRole);
        Role resultRole = mapper.toDomain(entity);

        assertThat(resultRole).isNotNull();
        assertThat(resultRole.getId()).isEqualTo(originalRole.getId());
        assertThat(resultRole.getName()).isEqualTo(originalRole.getName());
        assertThat(resultRole.getDescription()).isEqualTo(originalRole.getDescription());
        assertThat(resultRole.getCreatedAt()).isEqualTo(originalRole.getCreatedAt());
    }

    @Test
    void toDomain_shouldMapAllRoleTypes() {
        LocalDateTime now = LocalDateTime.now();

        RoleEntity admin = RoleEntity.builder()
                .id(1)
                .name("Administrador")
                .description("Admin role")
                .createdAt(now)
                .build();

        RoleEntity operator = RoleEntity.builder()
                .id(2)
                .name("Operador Sucursal")
                .description("Branch operator")
                .createdAt(now)
                .build();

        RoleEntity backoffice = RoleEntity.builder()
                .id(3)
                .name("Operador Back Office")
                .description("Back office")
                .createdAt(now)
                .build();

        RoleEntity client = RoleEntity.builder()
                .id(4)
                .name("Cliente")
                .description("Customer")
                .createdAt(now)
                .build();

        RoleEntity fleet = RoleEntity.builder()
                .id(5)
                .name("Administrador Flotilla")
                .description("Fleet admin")
                .createdAt(now)
                .build();

        Role adminRole = mapper.toDomain(admin);
        Role operatorRole = mapper.toDomain(operator);
        Role backofficeRole = mapper.toDomain(backoffice);
        Role clientRole = mapper.toDomain(client);
        Role fleetRole = mapper.toDomain(fleet);

        assertThat(adminRole.getId()).isEqualTo(1);
        assertThat(operatorRole.getId()).isEqualTo(2);
        assertThat(backofficeRole.getId()).isEqualTo(3);
        assertThat(clientRole.getId()).isEqualTo(4);
        assertThat(fleetRole.getId()).isEqualTo(5);
    }
}
