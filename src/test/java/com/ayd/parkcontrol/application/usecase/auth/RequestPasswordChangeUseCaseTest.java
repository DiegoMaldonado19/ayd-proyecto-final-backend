package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestPasswordChangeUseCaseTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaPasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RequestPasswordChangeUseCase requestPasswordChangeUseCase;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .firstName("John")
                .lastName("Doe")
                .requiresPasswordChange(false) // Usuario que ya no requiere primer cambio
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void execute_shouldSend2FACodeSuccessfully_whenValidUser() {
        // Given
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).invalidateAllTokensForUser(any(), any());
        when(tokenRepository.save(any(PasswordResetTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).send2FACode(anyString(), anyString());

        // When
        ApiResponse<Void> response = requestPasswordChangeUseCase.execute();

        // Then
        assertThat(response.getMessage()).isEqualTo("Se ha enviado un código de verificación a tu correo electrónico para confirmar el cambio de contraseña");
        
        // Verify token was invalidated and new token saved
        verify(tokenRepository).invalidateAllTokensForUser(eq(1L), any(LocalDateTime.class));
        
        ArgumentCaptor<PasswordResetTokenEntity> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        
        PasswordResetTokenEntity savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUserId()).isEqualTo(1L);
        assertThat(savedToken.getToken()).hasSize(6);
        assertThat(savedToken.getIsUsed()).isFalse();
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now());
        
        // Verify 2FA code was sent
        verify(emailService).send2FACode(eq("test@parkcontrol.com"), eq(savedToken.getToken()));
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
        // Given
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> requestPasswordChangeUseCase.execute());
        verify(tokenRepository, never()).invalidateAllTokensForUser(any(), any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).send2FACode(anyString(), anyString());
    }

    @Test
    void execute_shouldThrowBusinessRuleException_whenUserRequiresFirstPasswordChange() {
        // Given
        testUser.setRequiresPasswordChange(true); // Usuario nuevo que requiere primer cambio
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.of(testUser));

        // When & Then
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, 
                () -> requestPasswordChangeUseCase.execute());
        assertThat(exception.getMessage()).isEqualTo("Para el primer cambio de contraseña utiliza el endpoint /auth/password/first-change");
        
        verify(tokenRepository, never()).invalidateAllTokensForUser(any(), any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).send2FACode(anyString(), anyString());
    }

    @Test
    void execute_shouldInvalidatePreviousTokens_beforeCreatingNew() {
        // Given
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).invalidateAllTokensForUser(any(), any());
        when(tokenRepository.save(any(PasswordResetTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).send2FACode(anyString(), anyString());

        // When
        requestPasswordChangeUseCase.execute();

        // Then
        verify(tokenRepository).invalidateAllTokensForUser(eq(1L), any(LocalDateTime.class));
        verify(tokenRepository).save(any(PasswordResetTokenEntity.class));
    }

    @Test
    void execute_shouldUse2FAEmailTemplate_notPasswordResetTemplate() {
        // Given
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).invalidateAllTokensForUser(any(), any());
        when(tokenRepository.save(any(PasswordResetTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).send2FACode(anyString(), anyString());

        // When
        requestPasswordChangeUseCase.execute();

        // Then
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).send2FACode(eq("test@parkcontrol.com"), codeCaptor.capture());
        
        String sentCode = codeCaptor.getValue();
        assertThat(sentCode).hasSize(6);
        assertThat(sentCode).matches("\\d{6}"); // Solo dígitos
        
        // Verify that sendPasswordResetEmail was NOT called
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }
}