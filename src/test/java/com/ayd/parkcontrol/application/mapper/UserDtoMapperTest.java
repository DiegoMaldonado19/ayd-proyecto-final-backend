package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.request.admin.CreateUserRequest;
import com.ayd.parkcontrol.application.dto.request.admin.UpdateUserRequest;
import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoMapperTest {

    private UserDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserDtoMapper();
    }

    @Test
    void toDomain_shouldMapCorrectly() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setFirst_name("Juan");
        request.setLast_name("Pérez");
        request.setPhone("12345678");
        request.setRole_type_id(1);
        request.setIs_active(true);

        User result = mapper.toDomain(request, "hashedPassword123");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("hashedPassword123");
        assertThat(result.getFirstName()).isEqualTo("Juan");
        assertThat(result.getLastName()).isEqualTo("Pérez");
        assertThat(result.getPhone()).isEqualTo("12345678");
        assertThat(result.getRoleTypeId()).isEqualTo(1);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getRequiresPasswordChange()).isTrue();
        assertThat(result.getHas2faEnabled()).isFalse();
        assertThat(result.getFailedLoginAttempts()).isEqualTo(0);
    }

    @Test
    void toDomain_withNullIsActive_shouldDefaultToTrue() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setFirst_name("Juan");
        request.setLast_name("Pérez");
        request.setRole_type_id(1);
        request.setIs_active(null);

        User result = mapper.toDomain(request, "hashedPassword123");

        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void updateDomain_shouldUpdateOnlyProvidedFields() {
        User user = User.builder()
                .email("old@example.com")
                .firstName("Old")
                .lastName("User")
                .phone("11111111")
                .roleTypeId(1)
                .build();

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@example.com");
        request.setFirst_name("New");
        // lastName, phone, roleTypeId not set (null)

        mapper.updateDomain(user, request);

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getLastName()).isEqualTo("User"); // unchanged
        assertThat(user.getPhone()).isEqualTo("11111111"); // unchanged
        assertThat(user.getRoleTypeId()).isEqualTo(1); // unchanged
    }

    @Test
    void updateDomain_withAllFields_shouldUpdateAll() {
        User user = User.builder()
                .email("old@example.com")
                .firstName("Old")
                .lastName("User")
                .phone("11111111")
                .roleTypeId(1)
                .build();

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@example.com");
        request.setFirst_name("Juan");
        request.setLast_name("Pérez");
        request.setPhone("99999999");
        request.setRole_type_id(2);

        mapper.updateDomain(user, request);

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getFirstName()).isEqualTo("Juan");
        assertThat(user.getLastName()).isEqualTo("Pérez");
        assertThat(user.getPhone()).isEqualTo("99999999");
        assertThat(user.getRoleTypeId()).isEqualTo(2);
    }

    @Test
    void toResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastLogin = now.minusDays(1);

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Juan")
                .lastName("Pérez")
                .phone("12345678")
                .roleTypeId(1)
                .isActive(true)
                .requiresPasswordChange(false)
                .has2faEnabled(true)
                .failedLoginAttempts(0)
                .lockedUntil(null)
                .lastLogin(lastLogin)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Role role = Role.builder()
                .id(1)
                .name("Administrador")
                .build();

        UserResponse result = mapper.toResponse(user, role);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirst_name()).isEqualTo("Juan");
        assertThat(result.getLast_name()).isEqualTo("Pérez");
        assertThat(result.getPhone()).isEqualTo("12345678");
        assertThat(result.getRole_type_id()).isEqualTo(1);
        assertThat(result.getRole_name()).isEqualTo("Administrador");
        assertThat(result.getIs_active()).isTrue();
        assertThat(result.getRequires_password_change()).isFalse();
        assertThat(result.getHas_2fa_enabled()).isTrue();
        assertThat(result.getFailed_login_attempts()).isEqualTo(0);
        assertThat(result.getLocked_until()).isNull();
        assertThat(result.getLast_login()).isEqualTo(lastLogin);
        assertThat(result.getCreated_at()).isEqualTo(now);
        assertThat(result.getUpdated_at()).isEqualTo(now);
    }

    @Test
    void toResponse_withNullRole_shouldMapWithoutRoleName() {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Juan")
                .lastName("Pérez")
                .roleTypeId(1)
                .isActive(true)
                .createdAt(now)
                .build();

        UserResponse result = mapper.toResponse(user, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRole_name()).isNull();
    }
}
