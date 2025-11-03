package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.admin.RoleResponse;
import com.ayd.parkcontrol.domain.model.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RoleDtoMapperTest {

    private RoleDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RoleDtoMapper();
    }

    @Test
    void toResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Role role = Role.builder()
                .id(1)
                .name("Administrador")
                .description("Administrador del sistema")
                .createdAt(now)
                .build();

        RoleResponse result = mapper.toResponse(role);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Administrador");
        assertThat(result.getDescription()).isEqualTo("Administrador del sistema");
        assertThat(result.getCreated_at()).isEqualTo(now);
    }
}
