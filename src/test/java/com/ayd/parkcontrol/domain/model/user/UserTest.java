package com.ayd.parkcontrol.domain.model.user;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para el modelo de dominio User.
 */
class UserTest {

    @Test
    void builder_shouldCreateUserCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .passwordHash("hashed_password")
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .roleTypeId(2)
                .isActive(true)
                .requiresPasswordChange(false)
                .has2faEnabled(true)
                .failedLoginAttempts(0)
                .lockedUntil(null)
                .lastLogin(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed_password");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getPhone()).isEqualTo("1234567890");
        assertThat(user.getRoleTypeId()).isEqualTo(2);
        assertThat(user.getIsActive()).isTrue();
        assertThat(user.getRequiresPasswordChange()).isFalse();
        assertThat(user.getHas2faEnabled()).isTrue();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.getLastLogin()).isEqualTo(now);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        User user = new User();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPasswordHash()).isNull();
        assertThat(user.getFirstName()).isNull();
        assertThat(user.getLastName()).isNull();
        assertThat(user.getPhone()).isNull();
        assertThat(user.getRoleTypeId()).isNull();
        assertThat(user.getIsActive()).isNull();
    }

    @Test
    void allArgsConstructor_shouldCreateObjectWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User(
                1L,
                "test@test.com",
                "password",
                "Jane",
                "Smith",
                "9876543210",
                1,
                true,
                true,
                false,
                2,
                now.plusHours(1),
                now.minusDays(1),
                now,
                now);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getRoleTypeId()).isEqualTo(1);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(2);
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        user.setId(10L);
        user.setEmail("newemail@example.com");
        user.setPasswordHash("new_hash");
        user.setFirstName("Alice");
        user.setLastName("Johnson");
        user.setPhone("5555555555");
        user.setRoleTypeId(3);
        user.setIsActive(false);
        user.setRequiresPasswordChange(true);
        user.setHas2faEnabled(false);
        user.setFailedLoginAttempts(3);
        user.setLockedUntil(now.plusHours(2));
        user.setLastLogin(now.minusHours(1));
        user.setCreatedAt(now.minusDays(10));
        user.setUpdatedAt(now);

        assertThat(user.getId()).isEqualTo(10L);
        assertThat(user.getEmail()).isEqualTo("newemail@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("new_hash");
        assertThat(user.getFirstName()).isEqualTo("Alice");
        assertThat(user.getLastName()).isEqualTo("Johnson");
        assertThat(user.getPhone()).isEqualTo("5555555555");
        assertThat(user.getRoleTypeId()).isEqualTo(3);
        assertThat(user.getIsActive()).isFalse();
        assertThat(user.getRequiresPasswordChange()).isTrue();
        assertThat(user.getHas2faEnabled()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(user.getLockedUntil()).isEqualTo(now.plusHours(2));
        assertThat(user.getLastLogin()).isEqualTo(now.minusHours(1));
        assertThat(user.getCreatedAt()).isEqualTo(now.minusDays(10));
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void equals_shouldReturnTrue_forSameValues() {
        User user1 = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        User user2 = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        assertThat(user1).isEqualTo(user2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        User user1 = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();

        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void toString_shouldContainKeyFields() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        String toString = user.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("email=user@example.com");
        assertThat(toString).contains("firstName=John");
        assertThat(toString).contains("lastName=Doe");
        assertThat(toString).contains("isActive=true");
    }
}
