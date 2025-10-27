package com.ayd.parkcontrol.infrastructure.ratelimit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RateLimitService.
 */
@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimitService service;

    @Test
    void isRateLimitExceeded_firstRequest_shouldReturnFalse() {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String ip = "192.168.1.1";
        String key = "rate_limit:/api/v1/auth/login:192.168.1.1";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        // Act
        boolean result = service.isRateLimitExceeded(endpoint, ip, 5, 900);

        // Assert
        assertFalse(result);
        verify(valueOperations).set(key, "1", Duration.ofSeconds(900));
    }

    @Test
    void isRateLimitExceeded_belowLimit_shouldReturnFalse() {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String ip = "192.168.1.1";
        String key = "rate_limit:/api/v1/auth/login:192.168.1.1";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("3");
        when(valueOperations.increment(key)).thenReturn(4L);

        // Act
        boolean result = service.isRateLimitExceeded(endpoint, ip, 5, 900);

        // Assert
        assertFalse(result);
        verify(valueOperations).increment(key);
    }

    @Test
    void isRateLimitExceeded_atLimit_shouldReturnTrue() {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String ip = "192.168.1.1";
        String key = "rate_limit:/api/v1/auth/login:192.168.1.1";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("5");

        // Act
        boolean result = service.isRateLimitExceeded(endpoint, ip, 5, 900);

        // Assert
        assertTrue(result);
        verify(valueOperations, never()).increment(key);
    }

    @Test
    void isRateLimitExceeded_aboveLimit_shouldReturnTrue() {
        // Arrange
        String endpoint = "/api/v1/auth/verify-2fa";
        String ip = "10.0.0.1";
        String key = "rate_limit:/api/v1/auth/verify-2fa:10.0.0.1";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("10");

        // Act
        boolean result = service.isRateLimitExceeded(endpoint, ip, 3, 300);

        // Assert
        assertTrue(result);
    }

    @Test
    void getRemainingLockTime_withActiveLock_shouldReturnSeconds() {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String ip = "192.168.1.1";
        String key = "rate_limit:/api/v1/auth/login:192.168.1.1";

        when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(450L);

        // Act
        long result = service.getRemainingLockTime(endpoint, ip);

        // Assert
        assertEquals(450L, result);
    }

    @Test
    void getRemainingLockTime_withoutLock_shouldReturnZero() {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String ip = "192.168.1.1";
        String key = "rate_limit:/api/v1/auth/login:192.168.1.1";

        when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(-1L);

        // Act
        long result = service.getRemainingLockTime(endpoint, ip);

        // Assert
        assertEquals(0L, result);
    }

    @Test
    void resetLimit_shouldDeleteKey() {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String ip = "192.168.1.1";
        String key = "rate_limit:/api/v1/auth/login:192.168.1.1";

        // Act
        service.resetLimit(endpoint, ip);

        // Assert
        verify(redisTemplate).delete(key);
    }
}
