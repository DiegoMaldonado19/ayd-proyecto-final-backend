package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.response.branch.OccupancyResponse;
import com.ayd.parkcontrol.domain.exception.BranchNotFoundException;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GetOccupancyUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private GetOccupancyUseCase getOccupancyUseCase;

    private Branch branch;

    @BeforeEach
    void setUp() {
        branch = Branch.builder()
                .id(1L)
                .name("Centro")
                .capacity2r(50)
                .capacity4r(30)
                .build();

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void execute_WithExistingBranchAndOccupancyData_ShouldReturnOccupancyResponse() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(valueOperations.get("branch:occupancy:1:2R")).thenReturn(20);
        when(valueOperations.get("branch:occupancy:1:4R")).thenReturn(10);

        // Act
        OccupancyResponse result = getOccupancyUseCase.execute(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBranchId()).isEqualTo(1L);
        assertThat(result.getBranchName()).isEqualTo("Centro");
        
        // 2R capacity assertions
        assertThat(result.getCapacity2r()).isEqualTo(50);
        assertThat(result.getOccupied2r()).isEqualTo(20);
        assertThat(result.getAvailable2r()).isEqualTo(30);
        assertThat(result.getOccupancyPercentage2r()).isEqualTo(40.0);
        
        // 4R capacity assertions
        assertThat(result.getCapacity4r()).isEqualTo(30);
        assertThat(result.getOccupied4r()).isEqualTo(10);
        assertThat(result.getAvailable4r()).isEqualTo(20);
        assertThat(result.getOccupancyPercentage4r()).isCloseTo(33.33, within(0.01));
        
        // Total assertions
        assertThat(result.getTotalCapacity()).isEqualTo(80);
        assertThat(result.getTotalOccupied()).isEqualTo(30);
        assertThat(result.getTotalAvailable()).isEqualTo(50);
        assertThat(result.getTotalOccupancyPercentage()).isEqualTo(37.5);
        
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    void execute_WithNoOccupancyInRedis_ShouldReturnZeroOccupancy() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        OccupancyResponse result = getOccupancyUseCase.execute(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOccupied2r()).isEqualTo(0);
        assertThat(result.getOccupied4r()).isEqualTo(0);
        assertThat(result.getAvailable2r()).isEqualTo(50);
        assertThat(result.getAvailable4r()).isEqualTo(30);
        assertThat(result.getTotalOccupied()).isEqualTo(0);
        assertThat(result.getTotalAvailable()).isEqualTo(80);
    }

    @Test
    void execute_WithFullOccupancy_ShouldReturn100Percent() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(valueOperations.get("branch:occupancy:1:2R")).thenReturn(50);
        when(valueOperations.get("branch:occupancy:1:4R")).thenReturn(30);

        // Act
        OccupancyResponse result = getOccupancyUseCase.execute(1L);

        // Assert
        assertThat(result.getOccupied2r()).isEqualTo(50);
        assertThat(result.getAvailable2r()).isEqualTo(0);
        assertThat(result.getOccupancyPercentage2r()).isEqualTo(100.0);
        
        assertThat(result.getOccupied4r()).isEqualTo(30);
        assertThat(result.getAvailable4r()).isEqualTo(0);
        assertThat(result.getOccupancyPercentage4r()).isEqualTo(100.0);
        
        assertThat(result.getTotalOccupied()).isEqualTo(80);
        assertThat(result.getTotalAvailable()).isEqualTo(0);
        assertThat(result.getTotalOccupancyPercentage()).isEqualTo(100.0);
    }

    @Test
    void execute_WithNonExistentBranch_ShouldThrowBranchNotFoundException() {
        // Arrange
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getOccupancyUseCase.execute(999L))
                .isInstanceOf(BranchNotFoundException.class)
                .hasMessageContaining("Branch with ID 999 not found");

        verify(branchRepository).findById(999L);
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void execute_WithPartialOccupancy_ShouldCalculateCorrectPercentages() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(valueOperations.get("branch:occupancy:1:2R")).thenReturn(25); // 50%
        when(valueOperations.get("branch:occupancy:1:4R")).thenReturn(15); // 50%

        // Act
        OccupancyResponse result = getOccupancyUseCase.execute(1L);

        // Assert
        assertThat(result.getOccupancyPercentage2r()).isEqualTo(50.0);
        assertThat(result.getOccupancyPercentage4r()).isEqualTo(50.0);
        assertThat(result.getTotalOccupancyPercentage()).isEqualTo(50.0);
    }

    @Test
    void execute_WithStringValueFromRedis_ShouldParseCorrectly() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(valueOperations.get("branch:occupancy:1:2R")).thenReturn("15");
        when(valueOperations.get("branch:occupancy:1:4R")).thenReturn("8");

        // Act
        OccupancyResponse result = getOccupancyUseCase.execute(1L);

        // Assert
        assertThat(result.getOccupied2r()).isEqualTo(15);
        assertThat(result.getOccupied4r()).isEqualTo(8);
    }

    @Test
    void execute_WithLowOccupancy_ShouldReturnCorrectValues() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(valueOperations.get("branch:occupancy:1:2R")).thenReturn(2);
        when(valueOperations.get("branch:occupancy:1:4R")).thenReturn(1);

        // Act
        OccupancyResponse result = getOccupancyUseCase.execute(1L);

        // Assert
        assertThat(result.getOccupied2r()).isEqualTo(2);
        assertThat(result.getAvailable2r()).isEqualTo(48);
        assertThat(result.getOccupancyPercentage2r()).isEqualTo(4.0);
        
        assertThat(result.getOccupied4r()).isEqualTo(1);
        assertThat(result.getAvailable4r()).isEqualTo(29);
        assertThat(result.getOccupancyPercentage4r()).isCloseTo(3.33, within(0.01));
        
        assertThat(result.getTotalOccupied()).isEqualTo(3);
        assertThat(result.getTotalAvailable()).isEqualTo(77);
        assertThat(result.getTotalOccupancyPercentage()).isEqualTo(3.75);
    }

    @Test
    void execute_WithDifferentBranch_ShouldUseCorrectRedisKeys() {
        // Arrange
        Branch differentBranch = Branch.builder()
                .id(5L)
                .name("Norte")
                .capacity2r(100)
                .capacity4r(50)
                .build();

        when(branchRepository.findById(5L)).thenReturn(Optional.of(differentBranch));
        when(valueOperations.get("branch:occupancy:5:2R")).thenReturn(40);
        when(valueOperations.get("branch:occupancy:5:4R")).thenReturn(20);

        // Act
        OccupancyResponse result = getOccupancyUseCase.execute(5L);

        // Assert
        assertThat(result.getBranchId()).isEqualTo(5L);
        assertThat(result.getBranchName()).isEqualTo("Norte");
        assertThat(result.getCapacity2r()).isEqualTo(100);
        assertThat(result.getCapacity4r()).isEqualTo(50);
        
        verify(valueOperations).get("branch:occupancy:5:2R");
        verify(valueOperations).get("branch:occupancy:5:4R");
    }

    @Test
    void execute_WithNearlyFullOccupancy_ShouldHandleCorrectly() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(valueOperations.get("branch:occupancy:1:2R")).thenReturn(49);
        when(valueOperations.get("branch:occupancy:1:4R")).thenReturn(29);

        // Act
        OccupancyResponse result = getOccupancyUseCase.execute(1L);

        // Assert
        assertThat(result.getAvailable2r()).isEqualTo(1);
        assertThat(result.getAvailable4r()).isEqualTo(1);
        assertThat(result.getOccupancyPercentage2r()).isEqualTo(98.0);
        assertThat(result.getOccupancyPercentage4r()).isCloseTo(96.67, within(0.01));
    }

    private static org.assertj.core.data.Offset<Double> within(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}
