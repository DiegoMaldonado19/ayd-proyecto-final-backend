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
class Disable2FAUseCaseTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private Disable2FAUseCase disable2FAUseCase;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .has2faEnabled(true)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void execute_shouldDisable2FASuccessfully_when2FAIsEnabled() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).update2FAStatus(anyString(), eq(false));
        doNothing().when(twoFactorAuthService).invalidate2FACode(anyString());

        TwoFAResponse response = disable2FAUseCase.execute();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("2FA disabled successfully");
        assertThat(response.getIsEnabled()).isFalse();
        assertThat(response.getEmail()).contains("@parkcontrol.com");

        verify(userRepository).update2FAStatus(eq("test@parkcontrol.com"), eq(false));
        verify(twoFactorAuthService).invalidate2FACode(eq("test@parkcontrol.com"));
    }

    @Test
    void execute_shouldReturnAlreadyDisabledMessage_when2FAIsAlreadyDisabled() {
        testUser.setHas2faEnabled(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        TwoFAResponse response = disable2FAUseCase.execute();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("2FA is already disabled");
        assertThat(response.getIsEnabled()).isFalse();

        verify(userRepository, never()).update2FAStatus(anyString(), anyBoolean());
        verify(twoFactorAuthService, never()).invalidate2FACode(anyString());
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> disable2FAUseCase.execute())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).update2FAStatus(anyString(), anyBoolean());
    }

    @Test
    void execute_shouldMaskEmailCorrectly_whenDisabling2FA() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).update2FAStatus(anyString(), eq(false));
        doNothing().when(twoFactorAuthService).invalidate2FACode(anyString());

        TwoFAResponse response = disable2FAUseCase.execute();

        assertThat(response.getEmail()).matches("t.*@parkcontrol\\.com");
        assertThat(response.getEmail()).contains("*");
    }
}
