package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.response.branch.BranchResponse;
import com.ayd.parkcontrol.application.mapper.BranchDtoMapper;
import com.ayd.parkcontrol.domain.exception.BranchNotFoundException;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetBranchUseCase Tests")
class GetBranchUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchDtoMapper branchDtoMapper;

    @InjectMocks
    private GetBranchUseCase getBranchUseCase;

    private Branch existingBranch;
    private BranchResponse expectedResponse;

    @BeforeEach
    void setUp() {
        existingBranch = Branch.builder()
                .id(1L)
                .name("Test Branch")
                .address("123 Test Street, Test City")
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(20, 0))
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(new BigDecimal("15.00"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        expectedResponse = BranchResponse.builder()
                .id(1L)
                .name("Test Branch")
                .address("123 Test Street, Test City")
                .openingTime("08:00")
                .closingTime("20:00")
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(new BigDecimal("15.00"))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should return branch when it exists")
    void shouldReturnBranchWhenExists() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(existingBranch));
        when(branchDtoMapper.toResponse(any(Branch.class))).thenReturn(expectedResponse);

        BranchResponse result = getBranchUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Branch");
        assertThat(result.getCapacity2r()).isEqualTo(50);
        assertThat(result.getCapacity4r()).isEqualTo(100);

        verify(branchRepository).findById(1L);
        verify(branchDtoMapper).toResponse(existingBranch);
    }

    @Test
    @DisplayName("Should throw BranchNotFoundException when branch does not exist")
    void shouldThrowExceptionWhenBranchNotExists() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getBranchUseCase.execute(999L))
                .isInstanceOf(BranchNotFoundException.class)
                .hasMessageContaining("Branch with ID 999 not found");

        verify(branchRepository).findById(999L);
        verify(branchDtoMapper, never()).toResponse(any(Branch.class));
    }

    @Test
    @DisplayName("Should handle different branch IDs correctly")
    void shouldHandleDifferentBranchIds() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(existingBranch));
        when(branchDtoMapper.toResponse(any(Branch.class))).thenReturn(expectedResponse);

        getBranchUseCase.execute(5L);
        getBranchUseCase.execute(10L);

        verify(branchRepository).findById(5L);
        verify(branchRepository).findById(10L);
        verify(branchDtoMapper, times(2)).toResponse(any(Branch.class));
    }
}
