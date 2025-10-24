package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.request.branch.UpdateBranchRequest;
import com.ayd.parkcontrol.application.dto.response.branch.BranchResponse;
import com.ayd.parkcontrol.application.mapper.BranchDtoMapper;
import com.ayd.parkcontrol.domain.exception.BranchNotFoundException;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.DuplicateBranchNameException;
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
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateBranchUseCase Tests")
class UpdateBranchUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchDtoMapper branchDtoMapper;

    @InjectMocks
    private UpdateBranchUseCase updateBranchUseCase;

    private Branch existingBranch;
    private UpdateBranchRequest updateRequest;
    private BranchResponse expectedResponse;

    @BeforeEach
    void setUp() {
        existingBranch = Branch.builder()
                .id(1L)
                .name("Old Branch Name")
                .address("Old Address")
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(20, 0))
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(new BigDecimal("15.00"))
                .isActive(true)
                .build();

        updateRequest = UpdateBranchRequest.builder()
                .name("New Branch Name")
                .address("New Address")
                .ratePerHour(new BigDecimal("20.00"))
                .build();

        expectedResponse = BranchResponse.builder()
                .id(1L)
                .name("New Branch Name")
                .address("New Address")
                .ratePerHour(new BigDecimal("20.00"))
                .build();
    }

    @Test
    @DisplayName("Should update branch successfully")
    void shouldUpdateBranchSuccessfully() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(existingBranch));
        when(branchRepository.existsByNameAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(branchRepository.save(any(Branch.class))).thenReturn(existingBranch);
        when(branchDtoMapper.toResponse(any(Branch.class))).thenReturn(expectedResponse);

        BranchResponse result = updateBranchUseCase.execute(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Branch Name");

        verify(branchRepository).findById(1L);
        verify(branchRepository).existsByNameAndIdNot("New Branch Name", 1L);
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    @DisplayName("Should throw exception when branch not found")
    void shouldThrowExceptionWhenBranchNotFound() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateBranchUseCase.execute(999L, updateRequest))
                .isInstanceOf(BranchNotFoundException.class)
                .hasMessageContaining("Branch with ID 999 not found");

        verify(branchRepository).findById(999L);
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    @DisplayName("Should throw exception when new name already exists")
    void shouldThrowExceptionWhenNewNameExists() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(existingBranch));
        when(branchRepository.existsByNameAndIdNot(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> updateBranchUseCase.execute(1L, updateRequest))
                .isInstanceOf(DuplicateBranchNameException.class)
                .hasMessageContaining("Branch with name 'New Branch Name' already exists");

        verify(branchRepository).existsByNameAndIdNot("New Branch Name", 1L);
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    @DisplayName("Should throw exception when closing time before opening time")
    void shouldThrowExceptionWhenInvalidSchedule() {
        UpdateBranchRequest invalidRequest = UpdateBranchRequest.builder()
                .openingTime("20:00")
                .closingTime("08:00")
                .build();

        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(existingBranch));

        assertThatThrownBy(() -> updateBranchUseCase.execute(1L, invalidRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Closing time must be after opening time");

        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    @DisplayName("Should update only provided fields")
    void shouldUpdateOnlyProvidedFields() {
        UpdateBranchRequest partialRequest = UpdateBranchRequest.builder()
                .address("Updated Address Only")
                .build();

        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(existingBranch));
        when(branchRepository.save(any(Branch.class))).thenReturn(existingBranch);
        when(branchDtoMapper.toResponse(any(Branch.class))).thenReturn(expectedResponse);

        updateBranchUseCase.execute(1L, partialRequest);

        verify(branchRepository).save(any(Branch.class));
        verify(branchRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
    }
}
