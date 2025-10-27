package com.ayd.parkcontrol.infrastructure.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RedisOccupancyService.
 */
@ExtendWith(MockitoExtension.class)
class RedisOccupancyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisOccupancyService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void tryReserveSpace_withAvailableSpace_shouldReturnTrue() {
        // Arrange
        Long branchId = 1L;
        String vehicleType = "2R";
        int capacity = 10;
        String key = "branch:occupancy:1:2R";

        when(valueOperations.increment(key)).thenReturn(5L); // Nueva ocupación: 5

        // Act
        boolean result = service.tryReserveSpace(branchId, vehicleType, capacity);

        // Assert
        assertTrue(result);
        verify(valueOperations).increment(key);
        verify(valueOperations, never()).decrement(key); // No rollback
    }

    @Test
    void tryReserveSpace_withFullCapacity_shouldReturnFalseAndRollback() {
        // Arrange
        Long branchId = 1L;
        String vehicleType = "4R";
        int capacity = 10;
        String key = "branch:occupancy:1:4R";

        when(valueOperations.increment(key)).thenReturn(11L); // Excede capacidad
        when(valueOperations.decrement(key)).thenReturn(10L);

        // Act
        boolean result = service.tryReserveSpace(branchId, vehicleType, capacity);

        // Assert
        assertFalse(result);
        verify(valueOperations).increment(key);
        verify(valueOperations).decrement(key); // Rollback ejecutado
    }

    @Test
    void tryReserveSpace_withExactCapacity_shouldReturnTrue() {
        // Arrange
        Long branchId = 1L;
        String vehicleType = "2R";
        int capacity = 10;
        String key = "branch:occupancy:1:2R";

        when(valueOperations.increment(key)).thenReturn(10L); // Exactamente en capacidad

        // Act
        boolean result = service.tryReserveSpace(branchId, vehicleType, capacity);

        // Assert
        assertTrue(result);
        verify(valueOperations).increment(key);
        verify(valueOperations, never()).decrement(key);
    }

    @Test
    void releaseSpace_shouldDecrementOccupancy() {
        // Arrange
        Long branchId = 1L;
        String vehicleType = "2R";
        String key = "branch:occupancy:1:2R";

        when(valueOperations.decrement(key)).thenReturn(4L);

        // Act
        service.releaseSpace(branchId, vehicleType);

        // Assert
        verify(valueOperations).decrement(key);
    }

    @Test
    void releaseSpace_withNegativeResult_shouldResetToZero() {
        // Arrange
        Long branchId = 1L;
        String vehicleType = "2R";
        String key = "branch:occupancy:1:2R";

        when(valueOperations.decrement(key)).thenReturn(-1L);

        // Act
        service.releaseSpace(branchId, vehicleType);

        // Assert
        verify(valueOperations).decrement(key);
        verify(valueOperations).set(key, "0"); // Ajuste a 0
    }

    @Test
    void getCurrentOccupancy_withExistingValue_shouldReturnValue() {
        // Arrange
        Long branchId = 1L;
        String vehicleType = "4R";
        String key = "branch:occupancy:1:4R";

        when(valueOperations.get(key)).thenReturn("7");

        // Act
        int result = service.getCurrentOccupancy(branchId, vehicleType);

        // Assert
        assertEquals(7, result);
    }

    @Test
    void getCurrentOccupancy_withNullValue_shouldReturnZero() {
        // Arrange
        Long branchId = 1L;
        String vehicleType = "2R";
        String key = "branch:occupancy:1:2R";

        when(valueOperations.get(key)).thenReturn(null);

        // Act
        int result = service.getCurrentOccupancy(branchId, vehicleType);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void setOccupancy_shouldSetValue() {
        // Arrange
        Long branchId = 1L;
        String vehicleType = "2R";
        int occupancy = 5;
        String key = "branch:occupancy:1:2R";

        // Act
        service.setOccupancy(branchId, vehicleType, occupancy);

        // Assert
        verify(valueOperations).set(key, "5");
    }

    @Test
    void setOccupancy_withNegative_shouldSetZero() {
        // Arrange
        Long branchId = 1L;
        String vehicleType = "4R";
        int occupancy = -3;
        String key = "branch:occupancy:1:4R";

        // Act
        service.setOccupancy(branchId, vehicleType, occupancy);

        // Assert
        verify(valueOperations).set(key, "0");
    }

    @Test
    void resetOccupancy_shouldSetToZero() {
        // Arrange
        Long branchId = 1L;
        String vehicleType = "2R";
        String key = "branch:occupancy:1:2R";

        // Act
        service.resetOccupancy(branchId, vehicleType);

        // Assert
        verify(valueOperations).set(key, "0");
    }
}
