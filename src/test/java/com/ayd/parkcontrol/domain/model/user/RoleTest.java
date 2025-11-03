package com.ayd.parkcontrol.domain.model.user;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para el modelo de dominio Role.
 */
class RoleTest {

    @Test
    void builder_shouldCreateRoleCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Role role = Role.builder()
                .id(1)
                .name("ADMIN")
                .description("Administrator role")
                .createdAt(now)
                .build();

        assertThat(role).isNotNull();
        assertThat(role.getId()).isEqualTo(1);
        assertThat(role.getName()).isEqualTo("ADMIN");
        assertThat(role.getDescription()).isEqualTo("Administrator role");
        assertThat(role.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        Role role = new Role();
        assertThat(role).isNotNull();
        assertThat(role.getId()).isNull();
        assertThat(role.getName()).isNull();
        assertThat(role.getDescription()).isNull();
        assertThat(role.getCreatedAt()).isNull();
    }

    @Test
    void allArgsConstructor_shouldCreateObjectWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Role role = new Role(2, "USER", "Regular user role", now);

        assertThat(role.getId()).isEqualTo(2);
        assertThat(role.getName()).isEqualTo("USER");
        assertThat(role.getDescription()).isEqualTo("Regular user role");
        assertThat(role.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        Role role = new Role();
        LocalDateTime now = LocalDateTime.now();

        role.setId(3);
        role.setName("MODERATOR");
        role.setDescription("Moderator role");
        role.setCreatedAt(now);

        assertThat(role.getId()).isEqualTo(3);
        assertThat(role.getName()).isEqualTo("MODERATOR");
        assertThat(role.getDescription()).isEqualTo("Moderator role");
        assertThat(role.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void equals_shouldReturnTrue_forSameValues() {
        Role role1 = Role.builder()
                .id(1)
                .name("ADMIN")
                .description("Admin role")
                .build();

        Role role2 = Role.builder()
                .id(1)
                .name("ADMIN")
                .description("Admin role")
                .build();

        assertThat(role1).isEqualTo(role2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        Role role1 = Role.builder()
                .id(1)
                .name("ADMIN")
                .build();

        Role role2 = Role.builder()
                .id(1)
                .name("ADMIN")
                .build();

        assertThat(role1.hashCode()).isEqualTo(role2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        Role role = Role.builder()
                .id(1)
                .name("ADMIN")
                .description("Administrator role")
                .build();

        String toString = role.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name=ADMIN");
        assertThat(toString).contains("description=Administrator role");
    }
}
