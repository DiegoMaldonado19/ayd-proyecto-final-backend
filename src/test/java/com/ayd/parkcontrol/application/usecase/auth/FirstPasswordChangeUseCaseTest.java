package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.FirstPasswordChangeRequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.InvalidCredentialsException;
import com.ayd.parkcontrol.domain.exception.PasswordMismatchException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirstPasswordChangeUseCaseTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FirstPasswordChangeUseCase firstPasswordChangeUseCase;

    private FirstPasswordChangeRequest request;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .passwordHash("$2a$10$oldPasswordHash")
                .firstName("John")
                .lastName("Doe")
                .requiresPasswordChange(true)
                .build();

        request = FirstPasswordChangeRequest.builder()
                .currentPassword("tempPassword123")
                .newPassword("newPassword456")
                .confirmPassword("newPassword456")
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void execute_shouldChangePasswordSuccessfully_whenValidRequest() {
        // Given
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("tempPassword123", "$2a$10$oldPasswordHash")).thenReturn(true);
        when(passwordEncoder.encode("newPassword456")).thenReturn("$2a$10$newPasswordHash");

        // When
        ApiResponse<Void> response = firstPasswordChangeUseCase.execute(request);

        // Then
        assertThat(response.getMessage()).isEqualTo("Contraseña cambiada exitosamente. Ahora puedes usar todas las funciones del sistema.");
        verify(userRepository).updatePassword("test@parkcontrol.com", "$2a$10$newPasswordHash");
        verify(emailService).sendPasswordChangedNotification("test@parkcontrol.com", "John Doe");
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
        // Given
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> firstPasswordChangeUseCase.execute(request));
        verify(userRepository, never()).updatePassword(anyString(), anyString());
        verify(emailService, never()).sendPasswordChangedNotification(anyString(), anyString());
    }

    @Test
    void execute_shouldThrowBusinessRuleException_whenUserDoesNotRequirePasswordChange() {
        // Given
        testUser.setRequiresPasswordChange(false);
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.of(testUser));

        // When & Then
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, 
                () -> firstPasswordChangeUseCase.execute(request));
        assertThat(exception.getMessage()).isEqualTo("El usuario no requiere cambio de contraseña. Utiliza el endpoint regular de cambio de contraseña.");
        verify(userRepository, never()).updatePassword(anyString(), anyString());
        verify(emailService, never()).sendPasswordChangedNotification(anyString(), anyString());
    }

    @Test
    void execute_shouldThrowInvalidCredentialsException_whenCurrentPasswordIncorrect() {
        // Given
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("tempPassword123", "$2a$10$oldPasswordHash")).thenReturn(false);

        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, 
                () -> firstPasswordChangeUseCase.execute(request));
        assertThat(exception.getMessage()).isEqualTo("La contraseña actual es incorrecta");
        verify(userRepository, never()).updatePassword(anyString(), anyString());
        verify(emailService, never()).sendPasswordChangedNotification(anyString(), anyString());
    }

    @Test
    void execute_shouldThrowPasswordMismatchException_whenPasswordsDoNotMatch() {
        // Given
        request.setConfirmPassword("differentPassword");
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("tempPassword123", "$2a$10$oldPasswordHash")).thenReturn(true);

        // When & Then
        PasswordMismatchException exception = assertThrows(PasswordMismatchException.class, 
                () -> firstPasswordChangeUseCase.execute(request));
        assertThat(exception.getMessage()).isEqualTo("La nueva contraseña y la confirmación no coinciden");
        verify(userRepository, never()).updatePassword(anyString(), anyString());
        verify(emailService, never()).sendPasswordChangedNotification(anyString(), anyString());
    }

    @Test
    void execute_shouldThrowPasswordMismatchException_whenNewPasswordSameAsCurrent() {
        // Given
        request.setNewPassword("tempPassword123");
        request.setConfirmPassword("tempPassword123");
        when(userRepository.findByEmail("test@parkcontrol.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("tempPassword123", "$2a$10$oldPasswordHash")).thenReturn(true);

        // When & Then
        PasswordMismatchException exception = assertThrows(PasswordMismatchException.class, 
                () -> firstPasswordChangeUseCase.execute(request));
        assertThat(exception.getMessage()).isEqualTo("La nueva contraseña debe ser diferente a la contraseña actual");
        verify(userRepository, never()).updatePassword(anyString(), anyString());
        verify(emailService, never()).sendPasswordChangedNotification(anyString(), anyString());
    }
}