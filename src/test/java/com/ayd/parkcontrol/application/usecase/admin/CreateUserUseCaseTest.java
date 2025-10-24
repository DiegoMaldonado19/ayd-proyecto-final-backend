package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.request.admin.CreateUserRequest;
import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.mapper.UserDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicateEmailException;
import com.ayd.parkcontrol.domain.exception.RoleNotFoundException;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserDtoMapper userDtoMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    private CreateUserRequest validRequest;
    private User mockUser;
    private Role mockRole;
    private UserResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRequest = CreateUserRequest.builder()
                .email("test@parkcontrol.com")
                .password("TempPass123!")
                .first_name("Test")
                .last_name("User")
                .phone("50012345678")
                .role_type_id(2)
                .is_active(true)
                .build();

        mockRole = Role.builder()
                .id(2)
                .name("Operador Sucursal")
                .description("Operador de sucursal para control de entradas/salidas")
                .build();

        mockUser = User.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .phone("50012345678")
                .roleTypeId(2)
                .isActive(true)
                .requiresPasswordChange(true)
                .has2faEnabled(false)
                .failedLoginAttempts(0)
                .build();

        mockResponse = UserResponse.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .first_name("Test")
                .last_name("User")
                .phone("50012345678")
                .role_type_id(2)
                .role_name("Operador Sucursal")
                .is_active(true)
                .requires_password_change(true)
                .has_2fa_enabled(false)
                .failed_login_attempts(0)
                .build();
    }

    @Test
    void execute_shouldCreateUser_whenValidRequest() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findById(any())).thenReturn(Optional.of(mockRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userDtoMapper.toDomain(any(CreateUserRequest.class), anyString())).thenReturn(mockUser);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userDtoMapper.toResponse(any(User.class), any(Role.class))).thenReturn(mockResponse);

        UserResponse result = createUserUseCase.execute(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@parkcontrol.com");
        assertThat(result.getFirst_name()).isEqualTo("Test");
        assertThat(result.getLast_name()).isEqualTo("User");
        assertThat(result.getRole_name()).isEqualTo("Operador Sucursal");

        verify(userRepository).existsByEmail("test@parkcontrol.com");
        verify(roleRepository).findById(2);
        verify(passwordEncoder).encode("TempPass123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void execute_shouldThrowDuplicateEmailException_whenEmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> createUserUseCase.execute(validRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("test@parkcontrol.com");

        verify(userRepository).existsByEmail("test@parkcontrol.com");
        verify(roleRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowRoleNotFoundException_whenRoleDoesNotExist() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createUserUseCase.execute(validRequest))
                .isInstanceOf(RoleNotFoundException.class);

        verify(userRepository).existsByEmail("test@parkcontrol.com");
        verify(roleRepository).findById(2);
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_shouldEncodePassword_beforeSavingUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findById(any())).thenReturn(Optional.of(mockRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userDtoMapper.toDomain(any(CreateUserRequest.class), anyString())).thenReturn(mockUser);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userDtoMapper.toResponse(any(User.class), any(Role.class))).thenReturn(mockResponse);

        createUserUseCase.execute(validRequest);

        verify(passwordEncoder).encode("TempPass123!");
        verify(userDtoMapper).toDomain(validRequest, "hashedPassword");
    }
}
