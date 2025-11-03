package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.ResetPasswordRequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
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
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newTemporaryHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        ApiResponse<Void> response = resetPasswordUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Password reset instructions sent to your email");

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        assertThat(savedUser.getRequiresPasswordChange()).isTrue();
        assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$10$newTemporaryHash");

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
    void execute_shouldGenerateTemporaryPasswordWithCorrectLength_whenResettingPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newTemporaryHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        ArgumentCaptor<String> emailContentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), emailContentCaptor.capture());

        resetPasswordUseCase.execute(request);

        String emailContent = emailContentCaptor.getValue();
        assertThat(emailContent).contains("Please use this temporary password to login:");

        // Extract temporary password from email content
        String tempPassword = emailContent.replace("Please use this temporary password to login: ", "");
        assertThat(tempPassword).hasSize(12);
        assertThat(tempPassword).matches("[A-Za-z0-9!@#$%]+");
    }

    @Test
    void execute_shouldSetRequiresPasswordChangeToTrue_whenResettingPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newTemporaryHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        resetPasswordUseCase.execute(request);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getRequiresPasswordChange()).isTrue();
    }
}
