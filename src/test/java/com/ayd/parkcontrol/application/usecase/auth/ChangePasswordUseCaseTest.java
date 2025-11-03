package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.ChangePasswordRequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.port.notification.EmailService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

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
    private ChangePasswordUseCase changePasswordUseCase;

    private UserEntity testUser;
    private ChangePasswordRequest request;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .passwordHash("$2a$10$oldPasswordHash")
                .firstName("John")
                .lastName("Doe")
                .build();

        request = ChangePasswordRequest.builder()
                .currentPassword("oldPassword123")
                .newPassword("newPassword456")
                .confirmPassword("newPassword456")
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void execute_shouldChangePasswordSuccessfully_whenDataIsValid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("oldPassword123"), anyString())).thenReturn(true);
        when(passwordEncoder.encode(eq("newPassword456"))).thenReturn("$2a$10$newPasswordHash");
        doNothing().when(userRepository).updatePassword(anyString(), anyString());
        doNothing().when(emailService).sendPasswordChangedNotification(anyString(), anyString());

        ApiResponse<Void> response = changePasswordUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Password changed successfully");

        verify(userRepository).updatePassword(eq("test@parkcontrol.com"), eq("$2a$10$newPasswordHash"));
        verify(emailService).sendPasswordChangedNotification(eq("test@parkcontrol.com"), eq("John Doe"));
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> changePasswordUseCase.execute(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void execute_shouldThrowInvalidCredentialsException_whenCurrentPasswordIsIncorrect() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("oldPassword123"), anyString())).thenReturn(false);

        assertThatThrownBy(() -> changePasswordUseCase.execute(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Current password is incorrect");

        verify(userRepository, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void execute_shouldThrowPasswordMismatchException_whenNewPasswordsDoNotMatch() {
        request.setConfirmPassword("differentPassword");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("oldPassword123"), anyString())).thenReturn(true);

        assertThatThrownBy(() -> changePasswordUseCase.execute(request))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage("New password and confirmation do not match");

        verify(userRepository, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void execute_shouldThrowPasswordMismatchException_whenNewPasswordIsSameAsCurrentPassword() {
        request.setNewPassword("oldPassword123");
        request.setConfirmPassword("oldPassword123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("oldPassword123"), anyString())).thenReturn(true);

        assertThatThrownBy(() -> changePasswordUseCase.execute(request))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage("New password must be different from current password");

        verify(userRepository, never()).updatePassword(anyString(), anyString());
    }
}
