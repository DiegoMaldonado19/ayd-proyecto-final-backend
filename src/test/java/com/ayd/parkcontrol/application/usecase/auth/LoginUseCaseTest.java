package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.LoginRequest;
import com.ayd.parkcontrol.application.dto.response.auth.LoginResponse;
import com.ayd.parkcontrol.domain.exception.AccountLockedException;
import com.ayd.parkcontrol.domain.exception.InvalidCredentialsException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import com.ayd.parkcontrol.security.twofa.TwoFactorAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private UserEntity testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loginUseCase, "jwtExpirationMs", 3600000L);

        testUser = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .roleTypeId(1)
                .isActive(true)
                .has2faEnabled(false)
                .failedLoginAttempts(0)
                .requiresPasswordChange(false)
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@parkcontrol.com")
                .password("password123")
                .build();
    }

    @Test
    void execute_shouldLoginSuccessfully_whenCredentialsAreValid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyInt(), anyString())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");

        LoginResponse response = loginUseCase.execute(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmail()).isEqualTo("test@parkcontrol.com");
        assertThat(response.getRequires2faVerification()).isFalse();

        verify(userRepository).updateFailedLoginAttempts(anyString(), eq(0));
        verify(userRepository).updateLastLogin(anyString(), any(LocalDateTime.class));
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.execute(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void execute_shouldThrowInvalidCredentialsException_whenPasswordIsInvalid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.execute(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).updateFailedLoginAttempts(anyString(), eq(1));
    }

    @Test
    void execute_shouldThrowInvalidCredentialsException_whenUserIsInactive() {
        testUser.setIsActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> loginUseCase.execute(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Account is inactive");
    }

    @Test
    void execute_shouldThrowAccountLockedException_whenAccountIsLocked() {
        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> loginUseCase.execute(loginRequest))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Account is locked until");
    }

    @Test
    void execute_shouldRequire2FAVerification_when2FAIsEnabled() {
        testUser.setHas2faEnabled(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(twoFactorAuthService.generateAndSend2FACode(anyString())).thenReturn("123456");

        LoginResponse response = loginUseCase.execute(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getRequires2faVerification()).isTrue();
        assertThat(response.getHas2faEnabled()).isTrue();

        verify(twoFactorAuthService).generateAndSend2FACode(anyString());
    }

    @Test
    void execute_shouldLockAccount_whenMaxFailedAttemptsReached() {
        testUser.setFailedLoginAttempts(4);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.execute(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository).updateFailedLoginAttempts(anyString(), eq(5));
        verify(userRepository).lockAccount(anyString(), any(LocalDateTime.class));
    }
}
