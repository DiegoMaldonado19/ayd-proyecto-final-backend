package com.ayd.parkcontrol.security.twofa;

import com.ayd.parkcontrol.application.port.cache.CacheService;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoFactorAuthServiceTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TwoFactorAuthService twoFactorAuthService;

    private static final String TEST_EMAIL = "test@parkcontrol.com";

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    void generateAndSend2FACode_shouldGenerateAndSendCode() {
        String code = twoFactorAuthService.generateAndSend2FACode(TEST_EMAIL);

        assertThat(code).isNotNull();
        assertThat(code).hasSize(6);
        assertThat(code).matches("\\d{6}");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

        verify(cacheService).save(keyCaptor.capture(), eq(code), durationCaptor.capture());
        verify(emailService).send2FACode(TEST_EMAIL, code);

        assertThat(keyCaptor.getValue()).isEqualTo("2FA:" + TEST_EMAIL);
        assertThat(durationCaptor.getValue()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void verify2FACode_shouldReturnTrue_whenCodeIsValid() {
        String validCode = "123456";
        when(cacheService.get(anyString(), eq(String.class))).thenReturn(Optional.of(validCode));

        boolean result = twoFactorAuthService.verify2FACode(TEST_EMAIL, validCode);

        assertThat(result).isTrue();
        verify(cacheService).delete("2FA:" + TEST_EMAIL);
    }

    @Test
    void verify2FACode_shouldReturnFalse_whenCodeIsInvalid() {
        String storedCode = "123456";
        String providedCode = "654321";
        when(cacheService.get(anyString(), eq(String.class))).thenReturn(Optional.of(storedCode));

        boolean result = twoFactorAuthService.verify2FACode(TEST_EMAIL, providedCode);

        assertThat(result).isFalse();
        verify(cacheService, never()).delete(anyString());
    }

    @Test
    void verify2FACode_shouldReturnFalse_whenCodeDoesNotExist() {
        when(cacheService.get(anyString(), eq(String.class))).thenReturn(Optional.empty());

        boolean result = twoFactorAuthService.verify2FACode(TEST_EMAIL, "123456");

        assertThat(result).isFalse();
        verify(cacheService, never()).delete(anyString());
    }

    @Test
    void invalidate2FACode_shouldDeleteCode() {
        twoFactorAuthService.invalidate2FACode(TEST_EMAIL);

        verify(cacheService).delete("2FA:" + TEST_EMAIL);
    }
}
