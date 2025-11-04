package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.ResetPasswordRequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPasswordResetTokenRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JpaPasswordResetTokenRepository tokenRepository;

    @InjectMocks
    private ResetPasswordUseCase resetPasswordUseCase;

    private UserEntity testUser;
    private ResetPasswordRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(resetPasswordUseCase, "contextPath", "/api/v1");

        testUser = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .passwordHash("$2a$10$oldPasswordHash")
                .firstName("John")
                .lastName("Doe")
                .requiresPasswordChange(false)
                .build();

        request = ResetPasswordRequest.builder()
                .email("test@parkcontrol.com")
                .build();
    }

    @Test
    void execute_shouldResetPasswordSuccessfully_whenUserExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).invalidateAllTokensForUser(any(), any());
        when(tokenRepository.save(any(PasswordResetTokenEntity.class))).thenReturn(new PasswordResetTokenEntity());
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        ApiResponse<Void> response = resetPasswordUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Un código de verificación ha sido enviado a tu correo electrónico");

        verify(tokenRepository).invalidateAllTokensForUser(eq(1L), any());

        ArgumentCaptor<PasswordResetTokenEntity> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        PasswordResetTokenEntity savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getToken()).hasSize(6);
        assertThat(savedToken.getToken()).matches("\\d{6}");

        verify(emailService).sendPasswordResetEmail(eq("test@parkcontrol.com"), anyString());
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resetPasswordUseCase.execute(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void execute_shouldGenerate2FACodeWithCorrectLength_whenResettingPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).invalidateAllTokensForUser(any(), any());
        when(tokenRepository.save(any(PasswordResetTokenEntity.class))).thenReturn(new PasswordResetTokenEntity());

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), codeCaptor.capture());

        resetPasswordUseCase.execute(request);

        String sentCode = codeCaptor.getValue();
        
        // Verify the code is 6 digits
        assertThat(sentCode).hasSize(6);
        assertThat(sentCode).matches("\\d{6}"); // Only digits
    }

    @Test
    void execute_shouldInvalidateOldTokensAndCreateNewToken_whenResettingPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).invalidateAllTokensForUser(any(), any());
        when(tokenRepository.save(any(PasswordResetTokenEntity.class))).thenReturn(new PasswordResetTokenEntity());
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        resetPasswordUseCase.execute(request);

        verify(tokenRepository).invalidateAllTokensForUser(eq(1L), any());

        ArgumentCaptor<PasswordResetTokenEntity> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        assertThat(tokenCaptor.getValue().getUserId()).isEqualTo(1L);
        assertThat(tokenCaptor.getValue().getIsUsed()).isFalse();
    }
}
