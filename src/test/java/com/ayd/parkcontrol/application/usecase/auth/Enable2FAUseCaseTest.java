package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.response.auth.TwoFAResponse;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import com.ayd.parkcontrol.security.twofa.TwoFactorAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Enable2FAUseCaseTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private Enable2FAUseCase enable2FAUseCase;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .has2faEnabled(false)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void execute_shouldEnable2FASuccessfully_when2FAIsNotEnabled() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).update2FAStatus(anyString(), eq(true));
        when(twoFactorAuthService.generateAndSend2FACode(anyString())).thenReturn("123456");

        TwoFAResponse response = enable2FAUseCase.execute();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("2FA enabled successfully");
        assertThat(response.getIsEnabled()).isTrue();
        assertThat(response.getEmail()).contains("@parkcontrol.com");
        assertThat(response.getEmail()).contains("*");

        verify(userRepository).update2FAStatus(eq("test@parkcontrol.com"), eq(true));
        verify(twoFactorAuthService).generateAndSend2FACode(eq("test@parkcontrol.com"));
    }

    @Test
    void execute_shouldReturnAlreadyEnabledMessage_when2FAIsAlreadyEnabled() {
        testUser.setHas2faEnabled(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        TwoFAResponse response = enable2FAUseCase.execute();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("2FA is already enabled");
        assertThat(response.getIsEnabled()).isTrue();

        verify(userRepository, never()).update2FAStatus(anyString(), anyBoolean());
        verify(twoFactorAuthService, never()).generateAndSend2FACode(anyString());
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enable2FAUseCase.execute())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).update2FAStatus(anyString(), anyBoolean());
    }

    @Test
    void execute_shouldMaskEmailCorrectly_whenEnabling2FA() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).update2FAStatus(anyString(), eq(true));
        when(twoFactorAuthService.generateAndSend2FACode(anyString())).thenReturn("123456");

        TwoFAResponse response = enable2FAUseCase.execute();

        assertThat(response.getEmail()).matches("t.*@parkcontrol\\.com");
        assertThat(response.getEmail()).contains("*");
    }
}
