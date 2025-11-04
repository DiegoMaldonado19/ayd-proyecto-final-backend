package com.ayd.parkcontrol.security;

import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CustomUserDetailsService.
 * Valida la carga de detalles de usuario para autenticación.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User activeUser;
    private User inactiveUser;
    private User userRequiringPasswordChange;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L)
                .email("active@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Active")
                .lastName("User")
                .roleTypeId(2)
                .isActive(true)
                .requiresPasswordChange(false)
                .has2faEnabled(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        inactiveUser = User.builder()
                .id(2L)
                .email("inactive@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Inactive")
                .lastName("User")
                .roleTypeId(2)
                .isActive(false)
                .requiresPasswordChange(false)
                .has2faEnabled(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRequiringPasswordChange = User.builder()
                .id(3L)
                .email("needschange@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("NeedsChange")
                .lastName("User")
                .roleTypeId(2)
                .isActive(true)
                .requiresPasswordChange(true)
                .has2faEnabled(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Mock role repository
        com.ayd.parkcontrol.domain.model.user.Role role = com.ayd.parkcontrol.domain.model.user.Role.builder()
                .id(2)
                .name("Operador Sucursal")
                .build();

        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));
        when(roleRepository.findById(2)).thenReturn(Optional.of(role));

        UserDetails userDetails = userDetailsService.loadUserByUsername("active@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("active@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$hashedPassword");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        verify(userRepository).findByEmail("active@example.com");
        verify(roleRepository).findById(2);
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: nonexistent@example.com");

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_shouldReturnDisabledUser_whenUserIsInactive() {
        // Mock role repository
        com.ayd.parkcontrol.domain.model.user.Role role = com.ayd.parkcontrol.domain.model.user.Role.builder()
                .id(2)
                .name("Operador Sucursal")
                .build();

        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveUser));
        when(roleRepository.findById(2)).thenReturn(Optional.of(role));

        UserDetails userDetails = userDetailsService.loadUserByUsername("inactive@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("inactive@example.com");
        assertThat(userDetails.isEnabled()).isFalse();
        assertThat(userDetails.isAccountNonLocked()).isFalse();
        verify(userRepository).findByEmail("inactive@example.com");
        verify(roleRepository).findById(2);
    }

    @Test
    void loadUserByUsername_shouldReturnUserWithExpiredCredentials_whenPasswordChangeRequired() {
        // Mock role repository
        com.ayd.parkcontrol.domain.model.user.Role role = com.ayd.parkcontrol.domain.model.user.Role.builder()
                .id(2)
                .name("Operador Sucursal")
                .build();

        when(userRepository.findByEmail("needschange@example.com"))
                .thenReturn(Optional.of(userRequiringPasswordChange));
        when(roleRepository.findById(2)).thenReturn(Optional.of(role));

        UserDetails userDetails = userDetailsService.loadUserByUsername("needschange@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("needschange@example.com");
        assertThat(userDetails.isCredentialsNonExpired()).isFalse();
        verify(userRepository).findByEmail("needschange@example.com");
        verify(roleRepository).findById(2);
    }

    @Test
    void loadUserByUsername_shouldIncludeCorrectRole() {
        // Mock role repository
        com.ayd.parkcontrol.domain.model.user.Role role = com.ayd.parkcontrol.domain.model.user.Role.builder()
                .id(2)
                .name("Operador Sucursal")
                .build();

        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));
        when(roleRepository.findById(2)).thenReturn(Optional.of(role));

        UserDetails userDetails = userDetailsService.loadUserByUsername("active@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).isNotEmpty();
        assertThat(userDetails.getAuthorities()).anyMatch(auth -> auth.getAuthority().equals("ROLE_Operador Sucursal"));
        verify(userRepository).findByEmail("active@example.com");
        verify(roleRepository).findById(2);
    }

    @Test
    void loadUserByUsername_shouldMapEmailAsUsername() {
        // Mock role repository
        com.ayd.parkcontrol.domain.model.user.Role role = com.ayd.parkcontrol.domain.model.user.Role.builder()
                .id(2)
                .name("Operador Sucursal")
                .build();

        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));
        when(roleRepository.findById(2)).thenReturn(Optional.of(role));

        UserDetails userDetails = userDetailsService.loadUserByUsername("active@example.com");

        assertThat(userDetails.getUsername()).isEqualTo(activeUser.getEmail());
        verify(userRepository).findByEmail("active@example.com");
        verify(roleRepository).findById(2);
    }

    @Test
    void loadUserByUsername_shouldMapPasswordHashAsPassword() {
        // Mock role repository
        com.ayd.parkcontrol.domain.model.user.Role role = com.ayd.parkcontrol.domain.model.user.Role.builder()
                .id(2)
                .name("Operador Sucursal")
                .build();

        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));
        when(roleRepository.findById(2)).thenReturn(Optional.of(role));

        UserDetails userDetails = userDetailsService.loadUserByUsername("active@example.com");

        assertThat(userDetails.getPassword()).isEqualTo(activeUser.getPasswordHash());
        verify(userRepository).findByEmail("active@example.com");
        verify(roleRepository).findById(2);
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenRoleNotFound() {
        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));
        when(roleRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("active@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Role not found for user: active@example.com");

        verify(userRepository).findByEmail("active@example.com");
        verify(roleRepository).findById(2);
    }
}
