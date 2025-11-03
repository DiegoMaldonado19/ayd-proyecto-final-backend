package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.response.branch.BranchCapacityResponse;
import com.ayd.parkcontrol.application.mapper.BranchDtoMapper;
import com.ayd.parkcontrol.domain.exception.BranchNotFoundException;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCapacityUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchDtoMapper branchDtoMapper;

    @InjectMocks
    private GetCapacityUseCase getCapacityUseCase;

    private Branch branch;
    private BranchCapacityResponse expectedResponse;

    @BeforeEach
    void setUp() {
        branch = Branch.builder()
                .id(1L)
                .name("Centro")
                .capacity2r(50)
                .capacity4r(30)
                .build();

        expectedResponse = BranchCapacityResponse.builder()
                .branchId(1L)
                .branchName("Centro")
                .capacity2r(50)
                .capacity4r(30)
                .totalCapacity(80)
                .build();
    }

    @Test
    void execute_WithExistingBranch_ShouldReturnCapacityResponse() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(branchDtoMapper.toCapacityResponse(branch)).thenReturn(expectedResponse);

        // Act
        BranchCapacityResponse result = getCapacityUseCase.execute(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBranchId()).isEqualTo(1L);
        assertThat(result.getBranchName()).isEqualTo("Centro");
        assertThat(result.getCapacity2r()).isEqualTo(50);
        assertThat(result.getCapacity4r()).isEqualTo(30);
        assertThat(result.getTotalCapacity()).isEqualTo(80);

        verify(branchRepository).findById(1L);
        verify(branchDtoMapper).toCapacityResponse(branch);
    }

    @Test
    void execute_WithNonExistentBranch_ShouldThrowBranchNotFoundException() {
        // Arrange
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getCapacityUseCase.execute(999L))
                .isInstanceOf(BranchNotFoundException.class)
                .hasMessageContaining("Branch with ID 999 not found");

        verify(branchRepository).findById(999L);
        verify(branchDtoMapper, never()).toCapacityResponse(any());
    }

    @Test
    void execute_WithDifferentBranch_ShouldReturnCorrectCapacity() {
        // Arrange
        Branch largerBranch = Branch.builder()
                .id(2L)
                .name("Norte")
                .capacity2r(100)
                .capacity4r(60)
                .build();

        BranchCapacityResponse largerResponse = BranchCapacityResponse.builder()
                .branchId(2L)
                .branchName("Norte")
                .capacity2r(100)
                .capacity4r(60)
                .totalCapacity(160)
                .build();

        when(branchRepository.findById(2L)).thenReturn(Optional.of(largerBranch));
        when(branchDtoMapper.toCapacityResponse(largerBranch)).thenReturn(largerResponse);

        // Act
        BranchCapacityResponse result = getCapacityUseCase.execute(2L);

        // Assert
        assertThat(result.getBranchId()).isEqualTo(2L);
        assertThat(result.getBranchName()).isEqualTo("Norte");
        assertThat(result.getCapacity2r()).isEqualTo(100);
        assertThat(result.getCapacity4r()).isEqualTo(60);
        assertThat(result.getTotalCapacity()).isEqualTo(160);
    }

    @Test
    void execute_WithSmallCapacityBranch_ShouldReturnCorrectCapacity() {
        // Arrange
        Branch smallBranch = Branch.builder()
                .id(3L)
                .name("Sur")
                .capacity2r(10)
                .capacity4r(5)
                .build();

        BranchCapacityResponse smallResponse = BranchCapacityResponse.builder()
                .branchId(3L)
                .branchName("Sur")
                .capacity2r(10)
                .capacity4r(5)
                .totalCapacity(15)
                .build();

        when(branchRepository.findById(3L)).thenReturn(Optional.of(smallBranch));
        when(branchDtoMapper.toCapacityResponse(smallBranch)).thenReturn(smallResponse);

        // Act
        BranchCapacityResponse result = getCapacityUseCase.execute(3L);

        // Assert
        assertThat(result.getTotalCapacity()).isEqualTo(15);
        assertThat(result.getCapacity2r()).isEqualTo(10);
        assertThat(result.getCapacity4r()).isEqualTo(5);
    }

    @Test
    void execute_ShouldCallRepositoryOnlyOnce() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(branchDtoMapper.toCapacityResponse(branch)).thenReturn(expectedResponse);

        // Act
        getCapacityUseCase.execute(1L);

        // Assert
        verify(branchRepository, times(1)).findById(1L);
        verify(branchDtoMapper, times(1)).toCapacityResponse(branch);
    }

    @Test
    void execute_WithValidBranch_ShouldNotModifyBranchEntity() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(branchDtoMapper.toCapacityResponse(branch)).thenReturn(expectedResponse);

        // Act
        getCapacityUseCase.execute(1L);

        // Assert
        verify(branchRepository, never()).save(any());
    }

    @Test
    void execute_MultipleCalls_ShouldReturnConsistentResults() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(branchDtoMapper.toCapacityResponse(branch)).thenReturn(expectedResponse);

        // Act
        BranchCapacityResponse result1 = getCapacityUseCase.execute(1L);
        BranchCapacityResponse result2 = getCapacityUseCase.execute(1L);

        // Assert
        assertThat(result1.getBranchId()).isEqualTo(result2.getBranchId());
        assertThat(result1.getCapacity2r()).isEqualTo(result2.getCapacity2r());
        assertThat(result1.getCapacity4r()).isEqualTo(result2.getCapacity4r());
        
        verify(branchRepository, times(2)).findById(1L);
    }

    @Test
    void execute_WithZeroCapacity_ShouldReturnZeroValues() {
        // Arrange
        Branch zeroBranch = Branch.builder()
                .id(4L)
                .name("Empty")
                .capacity2r(0)
                .capacity4r(0)
                .build();

        BranchCapacityResponse zeroResponse = BranchCapacityResponse.builder()
                .branchId(4L)
                .branchName("Empty")
                .capacity2r(0)
                .capacity4r(0)
                .totalCapacity(0)
                .build();

        when(branchRepository.findById(4L)).thenReturn(Optional.of(zeroBranch));
        when(branchDtoMapper.toCapacityResponse(zeroBranch)).thenReturn(zeroResponse);

        // Act
        BranchCapacityResponse result = getCapacityUseCase.execute(4L);

        // Assert
        assertThat(result.getCapacity2r()).isEqualTo(0);
        assertThat(result.getCapacity4r()).isEqualTo(0);
        assertThat(result.getTotalCapacity()).isEqualTo(0);
    }

    @Test
    void execute_WithLargeCapacity_ShouldHandleCorrectly() {
        // Arrange
        Branch largeBranch = Branch.builder()
                .id(5L)
                .name("Megaparking")
                .capacity2r(1000)
                .capacity4r(500)
                .build();

        BranchCapacityResponse largeResponse = BranchCapacityResponse.builder()
                .branchId(5L)
                .branchName("Megaparking")
                .capacity2r(1000)
                .capacity4r(500)
                .totalCapacity(1500)
                .build();

        when(branchRepository.findById(5L)).thenReturn(Optional.of(largeBranch));
        when(branchDtoMapper.toCapacityResponse(largeBranch)).thenReturn(largeResponse);

        // Act
        BranchCapacityResponse result = getCapacityUseCase.execute(5L);

        // Assert
        assertThat(result.getTotalCapacity()).isEqualTo(1500);
        assertThat(result.getCapacity2r()).isEqualTo(1000);
        assertThat(result.getCapacity4r()).isEqualTo(500);
    }
}
