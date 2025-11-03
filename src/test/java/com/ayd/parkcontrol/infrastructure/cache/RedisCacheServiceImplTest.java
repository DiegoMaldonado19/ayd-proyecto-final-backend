package com.ayd.parkcontrol.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RedisCacheServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class RedisCacheServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisCacheServiceImpl cacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void save_withValidData_shouldSaveToCache() {
        // Arrange
        String key = "test:key";
        String value = "test value";
        Duration ttl = Duration.ofMinutes(5);

        // Act
        cacheService.save(key, value, ttl);

        // Assert
        verify(valueOperations).set(key, value, ttl);
    }

    @Test
    void save_withException_shouldLogErrorAndNotThrow() {
        // Arrange
        String key = "test:key";
        String value = "test value";
        Duration ttl = Duration.ofMinutes(5);

        doThrow(new RuntimeException("Redis error"))
                .when(valueOperations).set(key, value, ttl);

        // Act & Assert - no exception should be thrown
        cacheService.save(key, value, ttl);

        verify(valueOperations).set(key, value, ttl);
    }

    @Test
    void get_withExistingKey_shouldReturnValue() {
        // Arrange
        String key = "test:key";
        String expectedValue = "test value";

        when(valueOperations.get(key)).thenReturn(expectedValue);

        // Act
        Optional<String> result = cacheService.get(key, String.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedValue);
        verify(valueOperations).get(key);
    }

    @Test
    void get_withNonExistingKey_shouldReturnEmpty() {
        // Arrange
        String key = "nonexistent:key";

        when(valueOperations.get(key)).thenReturn(null);

        // Act
        Optional<String> result = cacheService.get(key, String.class);

        // Assert
        assertThat(result).isEmpty();
        verify(valueOperations).get(key);
    }

    @Test
    void get_withTypeConversion_shouldConvertValue() {
        // Arrange
        String key = "test:key";
        Map<String, Object> rawValue = new HashMap<>();
        rawValue.put("id", 1);
        rawValue.put("name", "Test");

        TestDto expectedDto = new TestDto(1L, "Test");

        when(valueOperations.get(key)).thenReturn(rawValue);
        when(objectMapper.convertValue(rawValue, TestDto.class)).thenReturn(expectedDto);

        // Act
        Optional<TestDto> result = cacheService.get(key, TestDto.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Test");
        verify(objectMapper).convertValue(rawValue, TestDto.class);
    }

    @Test
    void get_withException_shouldReturnEmpty() {
        // Arrange
        String key = "test:key";

        when(valueOperations.get(key)).thenThrow(new RuntimeException("Redis error"));

        // Act
        Optional<String> result = cacheService.get(key, String.class);

        // Assert
        assertThat(result).isEmpty();
        verify(valueOperations).get(key);
    }

    @Test
    void get_withConversionException_shouldReturnEmpty() {
        // Arrange
        String key = "test:key";
        Map<String, Object> rawValue = new HashMap<>();

        when(valueOperations.get(key)).thenReturn(rawValue);
        when(objectMapper.convertValue(any(), eq(TestDto.class)))
                .thenThrow(new RuntimeException("Conversion error"));

        // Act
        Optional<TestDto> result = cacheService.get(key, TestDto.class);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void delete_withExistingKey_shouldDeleteFromCache() {
        // Arrange
        String key = "test:key";

        when(redisTemplate.delete(key)).thenReturn(true);

        // Act
        cacheService.delete(key);

        // Assert
        verify(redisTemplate).delete(key);
    }

    @Test
    void delete_withException_shouldLogErrorAndNotThrow() {
        // Arrange
        String key = "test:key";

        when(redisTemplate.delete(key)).thenThrow(new RuntimeException("Redis error"));

        // Act & Assert - no exception should be thrown
        cacheService.delete(key);

        verify(redisTemplate).delete(key);
    }

    @Test
    void exists_withExistingKey_shouldReturnTrue() {
        // Arrange
        String key = "test:key";

        when(redisTemplate.hasKey(key)).thenReturn(true);

        // Act
        boolean result = cacheService.exists(key);

        // Assert
        assertThat(result).isTrue();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void exists_withNonExistingKey_shouldReturnFalse() {
        // Arrange
        String key = "nonexistent:key";

        when(redisTemplate.hasKey(key)).thenReturn(false);

        // Act
        boolean result = cacheService.exists(key);

        // Assert
        assertThat(result).isFalse();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void exists_withNullResult_shouldReturnFalse() {
        // Arrange
        String key = "test:key";

        when(redisTemplate.hasKey(key)).thenReturn(null);

        // Act
        boolean result = cacheService.exists(key);

        // Assert
        assertThat(result).isFalse();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void exists_withException_shouldReturnFalse() {
        // Arrange
        String key = "test:key";

        when(redisTemplate.hasKey(key)).thenThrow(new RuntimeException("Redis error"));

        // Act
        boolean result = cacheService.exists(key);

        // Assert
        assertThat(result).isFalse();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void save_withComplexObject_shouldSerializeAndSave() {
        // Arrange
        String key = "test:complex";
        TestDto dto = new TestDto(1L, "Test");
        Duration ttl = Duration.ofHours(1);

        // Act
        cacheService.save(key, dto, ttl);

        // Assert
        verify(valueOperations).set(key, dto, ttl);
    }

    // Helper class for testing
    private static class TestDto {
        private final Long id;
        private final String name;

        public TestDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
