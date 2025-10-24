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

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteBranchUseCase Tests")
class DeleteBranchUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchDtoMapper branchDtoMapper;

    @InjectMocks
    private DeleteBranchUseCase deleteBranchUseCase;

    private Branch existingBranch;
    private BranchResponse expectedResponse;

    @BeforeEach
    void setUp() {
        existingBranch = Branch.builder()
                .id(1L)
                .name("Test Branch")
                .address("Test Address")
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(20, 0))
                .capacity2r(50)
                .capacity4r(100)
                .isActive(true)
                .build();

        expectedResponse = BranchResponse.builder()
                .id(1L)
                .name("Test Branch")
                .isActive(false)
                .build();
    }

    @Test
    @DisplayName("Should soft delete branch successfully")
    void shouldSoftDeleteBranchSuccessfully() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(existingBranch));
        when(branchRepository.save(any(Branch.class))).thenReturn(existingBranch);
        when(branchDtoMapper.toResponse(any(Branch.class))).thenReturn(expectedResponse);

        BranchResponse result = deleteBranchUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isFalse();

        verify(branchRepository).findById(1L);
        verify(branchRepository).save(any(Branch.class));
        verify(branchDtoMapper).toResponse(any(Branch.class));
    }

    @Test
    @DisplayName("Should throw exception when branch not found")
    void shouldThrowExceptionWhenBranchNotFound() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteBranchUseCase.execute(999L))
                .isInstanceOf(BranchNotFoundException.class)
                .hasMessageContaining("Branch with ID 999 not found");

        verify(branchRepository).findById(999L);
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    @DisplayName("Should handle already inactive branch")
    void shouldHandleAlreadyInactiveBranch() {
        existingBranch.setIsActive(false);

        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(existingBranch));
        when(branchRepository.save(any(Branch.class))).thenReturn(existingBranch);
        when(branchDtoMapper.toResponse(any(Branch.class))).thenReturn(expectedResponse);

        BranchResponse result = deleteBranchUseCase.execute(1L);

        assertThat(result).isNotNull();
        verify(branchRepository).save(any(Branch.class));
    }
}
