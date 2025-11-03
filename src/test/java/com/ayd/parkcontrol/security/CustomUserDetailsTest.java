package com.ayd.parkcontrol.security;

import com.ayd.parkcontrol.domain.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    private User user;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .requiresPasswordChange(false)
                .lockedUntil(null)
                .build();
    }

    @Test
    void getAuthorities_WithRoleWithoutPrefix_ShouldAddRolePrefix() {
        // Arrange
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

        // Assert
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_Administrador");
    }

    @Test
    void getAuthorities_WithRoleWithPrefix_ShouldKeepPrefix() {
        // Arrange
        customUserDetails = new CustomUserDetails(user, "ROLE_Cliente");

        // Act
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

        // Assert
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_Cliente");
    }

    @Test
    void getPassword_ShouldReturnPasswordHash() {
        // Arrange
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        String password = customUserDetails.getPassword();

        // Assert
        assertThat(password).isEqualTo("$2a$10$hashedPassword");
    }

    @Test
    void getUsername_ShouldReturnEmail() {
        // Arrange
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        String username = customUserDetails.getUsername();

        // Assert
        assertThat(username).isEqualTo("test@parkcontrol.com");
    }

    @Test
    void isAccountNonExpired_ShouldAlwaysReturnTrue() {
        // Arrange
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        boolean nonExpired = customUserDetails.isAccountNonExpired();

        // Assert
        assertThat(nonExpired).isTrue();
    }

    @Test
    void isAccountNonLocked_WithActiveUserAndNoLock_ShouldReturnTrue() {
        // Arrange
        user.setIsActive(true);
        user.setLockedUntil(null);
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        boolean nonLocked = customUserDetails.isAccountNonLocked();

        // Assert
        assertThat(nonLocked).isTrue();
    }

    @Test
    void isAccountNonLocked_WithInactiveUser_ShouldReturnFalse() {
        // Arrange
        user.setIsActive(false);
        user.setLockedUntil(null);
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        boolean nonLocked = customUserDetails.isAccountNonLocked();

        // Assert
        assertThat(nonLocked).isFalse();
    }

    @Test
    void isAccountNonLocked_WithLockedAccount_ShouldReturnFalse() {
        // Arrange
        user.setIsActive(true);
        user.setLockedUntil(LocalDateTime.now().plusHours(1));
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        boolean nonLocked = customUserDetails.isAccountNonLocked();

        // Assert
        assertThat(nonLocked).isFalse();
    }

    @Test
    void isAccountNonLocked_WithBothInactiveAndLocked_ShouldReturnFalse() {
        // Arrange
        user.setIsActive(false);
        user.setLockedUntil(LocalDateTime.now().plusHours(1));
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        boolean nonLocked = customUserDetails.isAccountNonLocked();

        // Assert
        assertThat(nonLocked).isFalse();
    }

    @Test
    void isCredentialsNonExpired_WithNoPasswordChangeRequired_ShouldReturnTrue() {
        // Arrange
        user.setRequiresPasswordChange(false);
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        boolean credentialsNonExpired = customUserDetails.isCredentialsNonExpired();

        // Assert
        assertThat(credentialsNonExpired).isTrue();
    }

    @Test
    void isCredentialsNonExpired_WithPasswordChangeRequired_ShouldReturnFalse() {
        // Arrange
        user.setRequiresPasswordChange(true);
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        boolean credentialsNonExpired = customUserDetails.isCredentialsNonExpired();

        // Assert
        assertThat(credentialsNonExpired).isFalse();
    }

    @Test
    void isEnabled_WithActiveUser_ShouldReturnTrue() {
        // Arrange
        user.setIsActive(true);
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        boolean enabled = customUserDetails.isEnabled();

        // Assert
        assertThat(enabled).isTrue();
    }

    @Test
    void isEnabled_WithInactiveUser_ShouldReturnFalse() {
        // Arrange
        user.setIsActive(false);
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        boolean enabled = customUserDetails.isEnabled();

        // Assert
        assertThat(enabled).isFalse();
    }

    @Test
    void getUserId_ShouldReturnUserId() {
        // Arrange
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        Long userId = customUserDetails.getUserId();

        // Assert
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    void getRoleNameWithoutPrefix_WithPrefixedRole_ShouldRemovePrefix() {
        // Arrange
        customUserDetails = new CustomUserDetails(user, "ROLE_Operador Back Office");

        // Act
        String roleWithoutPrefix = customUserDetails.getRoleNameWithoutPrefix();

        // Assert
        assertThat(roleWithoutPrefix).isEqualTo("Operador Back Office");
    }

    @Test
    void getRoleNameWithoutPrefix_WithoutPrefix_ShouldReturnOriginal() {
        // Arrange
        customUserDetails = new CustomUserDetails(user, "Cliente");

        // Act
        String roleWithoutPrefix = customUserDetails.getRoleNameWithoutPrefix();

        // Assert
        assertThat(roleWithoutPrefix).isEqualTo("Cliente");
    }

    @Test
    void getUser_ShouldReturnOriginalUser() {
        // Arrange
        customUserDetails = new CustomUserDetails(user, "Administrador");

        // Act
        User retrievedUser = customUserDetails.getUser();

        // Assert
        assertThat(retrievedUser).isEqualTo(user);
        assertThat(retrievedUser.getId()).isEqualTo(1L);
        assertThat(retrievedUser.getEmail()).isEqualTo("test@parkcontrol.com");
    }

    @Test
    void getRoleName_ShouldReturnOriginalRoleName() {
        // Arrange
        String roleName = "Operador Back Office";
        customUserDetails = new CustomUserDetails(user, roleName);

        // Act
        String retrievedRoleName = customUserDetails.getRoleName();

        // Assert
        assertThat(retrievedRoleName).isEqualTo(roleName);
    }

    @Test
    void customUserDetails_WithComplexScenario_ShouldWorkCorrectly() {
        // Arrange
        User complexUser = User.builder()
                .id(99L)
                .email("complex@parkcontrol.com")
                .passwordHash("$2a$10$complexHash")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .requiresPasswordChange(false)
                .lockedUntil(null)
                .build();
        
        customUserDetails = new CustomUserDetails(complexUser, "ROLE_Administrador");

        // Act & Assert
        assertThat(customUserDetails.getUsername()).isEqualTo("complex@parkcontrol.com");
        assertThat(customUserDetails.getUserId()).isEqualTo(99L);
        assertThat(customUserDetails.isEnabled()).isTrue();
        assertThat(customUserDetails.isAccountNonLocked()).isTrue();
        assertThat(customUserDetails.isCredentialsNonExpired()).isTrue();
        assertThat(customUserDetails.getAuthorities()).hasSize(1);
        assertThat(customUserDetails.getRoleNameWithoutPrefix()).isEqualTo("Administrador");
    }

    @Test
    void customUserDetails_WithAllRestrictions_ShouldReflectCorrectly() {
        // Arrange
        User restrictedUser = User.builder()
                .id(5L)
                .email("restricted@parkcontrol.com")
                .passwordHash("$2a$10$restrictedHash")
                .firstName("Restricted")
                .lastName("User")
                .isActive(false)
                .requiresPasswordChange(true)
                .lockedUntil(LocalDateTime.now().plusDays(7))
                .build();
        
        customUserDetails = new CustomUserDetails(restrictedUser, "Cliente");

        // Act & Assert
        assertThat(customUserDetails.isEnabled()).isFalse();
        assertThat(customUserDetails.isAccountNonLocked()).isFalse();
        assertThat(customUserDetails.isCredentialsNonExpired()).isFalse();
        assertThat(customUserDetails.getUsername()).isEqualTo("restricted@parkcontrol.com");
    }
}
