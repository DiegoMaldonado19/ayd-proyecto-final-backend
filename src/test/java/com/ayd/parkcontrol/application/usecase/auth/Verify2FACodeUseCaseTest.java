package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.Verify2FARequest;
import com.ayd.parkcontrol.application.dto.response.auth.LoginResponse;
import com.ayd.parkcontrol.domain.exception.InvalidTwoFactorCodeException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Verify2FACodeUseCaseTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private Verify2FACodeUseCase verify2FACodeUseCase;

    private UserEntity testUser;
    private Verify2FARequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(verify2FACodeUseCase, "jwtExpirationMs", 3600000L);

        testUser = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .firstName("John")
                .lastName("Doe")
                .roleTypeId(1)
                .has2faEnabled(true)
                .requiresPasswordChange(false)
                .build();

        request = Verify2FARequest.builder()
                .email("test@parkcontrol.com")
                .code("123456")
                .build();
    }

    @Test
    void execute_shouldVerify2FASuccessfully_whenCodeIsValid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(twoFactorAuthService.verify2FACode(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyInt(), anyString())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
        doNothing().when(userRepository).updateFailedLoginAttempts(anyString(), eq(0));
        doNothing().when(userRepository).updateLastLogin(anyString(), any(LocalDateTime.class));

        LoginResponse response = verify2FACodeUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmail()).isEqualTo("test@parkcontrol.com");
        assertThat(response.getHas2faEnabled()).isTrue();
        assertThat(response.getRequires2faVerification()).isFalse();
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getRole()).isEqualTo("ADMIN");

        verify(userRepository).updateFailedLoginAttempts(eq("test@parkcontrol.com"), eq(0));
        verify(userRepository).updateLastLogin(eq("test@parkcontrol.com"), any(LocalDateTime.class));
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verify2FACodeUseCase.execute(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(twoFactorAuthService, never()).verify2FACode(anyString(), anyString());
    }

    @Test
    void execute_shouldThrowInvalidTwoFactorCodeException_whenCodeIsInvalid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(twoFactorAuthService.verify2FACode(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> verify2FACodeUseCase.execute(request))
                .isInstanceOf(InvalidTwoFactorCodeException.class)
                .hasMessage("Invalid or expired 2FA code");

        verify(jwtTokenProvider, never()).generateToken(anyString(), anyInt(), anyString());
    }

    @Test
    void execute_shouldMapRoleCorrectly_forDifferentRoleTypes() {
        // Test BRANCH_OPERATOR role
        testUser.setRoleTypeId(2);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(twoFactorAuthService.verify2FACode(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyInt(), eq("BRANCH_OPERATOR"))).thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh");

        LoginResponse response = verify2FACodeUseCase.execute(request);

        assertThat(response.getRole()).isEqualTo("BRANCH_OPERATOR");
        verify(jwtTokenProvider).generateToken(anyString(), anyInt(), eq("BRANCH_OPERATOR"));
    }

    @Test
    void execute_shouldResetFailedLoginAttemptsToZero_whenCodeIsValid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(twoFactorAuthService.verify2FACode(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyInt(), anyString())).thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh");

        verify2FACodeUseCase.execute(request);

        verify(userRepository).updateFailedLoginAttempts(eq("test@parkcontrol.com"), eq(0));
    }

    @Test
    void execute_shouldUpdateLastLoginTimestamp_whenCodeIsValid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(twoFactorAuthService.verify2FACode(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyInt(), anyString())).thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh");

        verify2FACodeUseCase.execute(request);

        verify(userRepository).updateLastLogin(eq("test@parkcontrol.com"), any(LocalDateTime.class));
    }
}
