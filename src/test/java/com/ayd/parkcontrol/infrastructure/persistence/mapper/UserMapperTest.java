package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
    }

    @Test
    void toEntity_shouldReturnNull_whenUserIsNull() {
        UserEntity result = mapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldMapCorrectly_whenUserIsValid() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .passwordHash("hashedPassword123")
                .firstName("John")
                .lastName("Doe")
                .phone("50212345678")
                .roleTypeId(4)
                .isActive(true)
                .requiresPasswordChange(false)
                .has2faEnabled(true)
                .failedLoginAttempts(0)
                .lockedUntil(null)
                .lastLogin(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserEntity result = mapper.toEntity(user);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@parkcontrol.com");
        assertThat(result.getPasswordHash()).isEqualTo("hashedPassword123");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getPhone()).isEqualTo("50212345678");
        assertThat(result.getRoleTypeId()).isEqualTo(4);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getRequiresPasswordChange()).isFalse();
        assertThat(result.getHas2faEnabled()).isTrue();
        assertThat(result.getFailedLoginAttempts()).isZero();
        assertThat(result.getLockedUntil()).isNull();
        assertThat(result.getLastLogin()).isEqualTo(now);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        User result = mapper.toDomain(null);

        assertThat(result).isNull();
    }

    @Test
    void toDomain_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lockedUntil = now.plusHours(1);
        UserEntity entity = UserEntity.builder()
                .id(2L)
                .email("admin@parkcontrol.com")
                .passwordHash("hashedAdminPass")
                .firstName("Admin")
                .lastName("User")
                .phone("50245678901")
                .roleTypeId(1)
                .isActive(true)
                .requiresPasswordChange(true)
                .has2faEnabled(true)
                .failedLoginAttempts(2)
                .lockedUntil(lockedUntil)
                .lastLogin(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getEmail()).isEqualTo("admin@parkcontrol.com");
        assertThat(result.getPasswordHash()).isEqualTo("hashedAdminPass");
        assertThat(result.getFirstName()).isEqualTo("Admin");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getPhone()).isEqualTo("50245678901");
        assertThat(result.getRoleTypeId()).isEqualTo(1);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getRequiresPasswordChange()).isTrue();
        assertThat(result.getHas2faEnabled()).isTrue();
        assertThat(result.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(result.getLockedUntil()).isEqualTo(lockedUntil);
        assertThat(result.getLastLogin()).isEqualTo(now);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldHandleOptionalFields_whenTheyAreNull() {
        User user = User.builder()
                .id(3L)
                .email("minimal@parkcontrol.com")
                .passwordHash("hash123")
                .firstName("Min")
                .lastName("User")
                .phone(null)
                .roleTypeId(4)
                .isActive(true)
                .requiresPasswordChange(false)
                .has2faEnabled(false)
                .failedLoginAttempts(0)
                .lockedUntil(null)
                .lastLogin(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        UserEntity result = mapper.toEntity(user);

        assertThat(result).isNotNull();
        assertThat(result.getPhone()).isNull();
        assertThat(result.getLockedUntil()).isNull();
        assertThat(result.getLastLogin()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void toDomain_shouldHandleOptionalFields_whenTheyAreNull() {
        UserEntity entity = UserEntity.builder()
                .id(4L)
                .email("simple@parkcontrol.com")
                .passwordHash("simpleHash")
                .firstName("Simple")
                .lastName("User")
                .phone(null)
                .roleTypeId(4)
                .isActive(false)
                .requiresPasswordChange(false)
                .has2faEnabled(false)
                .failedLoginAttempts(0)
                .lockedUntil(null)
                .lastLogin(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        User result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getPhone()).isNull();
        assertThat(result.getLockedUntil()).isNull();
        assertThat(result.getLastLogin()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        LocalDateTime now = LocalDateTime.now();
        User originalUser = User.builder()
                .id(5L)
                .email("bidirectional@parkcontrol.com")
                .passwordHash("hashBidi")
                .firstName("Bidi")
                .lastName("User")
                .phone("50298765432")
                .roleTypeId(2)
                .isActive(true)
                .requiresPasswordChange(true)
                .has2faEnabled(false)
                .failedLoginAttempts(1)
                .lockedUntil(null)
                .lastLogin(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserEntity entity = mapper.toEntity(originalUser);
        User resultUser = mapper.toDomain(entity);

        assertThat(resultUser).isNotNull();
        assertThat(resultUser.getId()).isEqualTo(originalUser.getId());
        assertThat(resultUser.getEmail()).isEqualTo(originalUser.getEmail());
        assertThat(resultUser.getPasswordHash()).isEqualTo(originalUser.getPasswordHash());
        assertThat(resultUser.getFirstName()).isEqualTo(originalUser.getFirstName());
        assertThat(resultUser.getLastName()).isEqualTo(originalUser.getLastName());
        assertThat(resultUser.getPhone()).isEqualTo(originalUser.getPhone());
        assertThat(resultUser.getRoleTypeId()).isEqualTo(originalUser.getRoleTypeId());
        assertThat(resultUser.getIsActive()).isEqualTo(originalUser.getIsActive());
        assertThat(resultUser.getRequiresPasswordChange()).isEqualTo(originalUser.getRequiresPasswordChange());
        assertThat(resultUser.getHas2faEnabled()).isEqualTo(originalUser.getHas2faEnabled());
        assertThat(resultUser.getFailedLoginAttempts()).isEqualTo(originalUser.getFailedLoginAttempts());
    }
}
