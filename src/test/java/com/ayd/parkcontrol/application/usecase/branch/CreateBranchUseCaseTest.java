package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.request.branch.CreateBranchRequest;
import com.ayd.parkcontrol.application.dto.response.branch.BranchResponse;
import com.ayd.parkcontrol.application.mapper.BranchDtoMapper;
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
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateBranchUseCase Tests")
class CreateBranchUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchDtoMapper branchDtoMapper;

    @InjectMocks
    private CreateBranchUseCase createBranchUseCase;

    private CreateBranchRequest validRequest;
    private Branch savedBranch;
    private BranchResponse expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = CreateBranchRequest.builder()
                .name("Test Branch")
                .address("123 Test Street, Test City")
                .openingTime("08:00")
                .closingTime("20:00")
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(new BigDecimal("15.00"))
                .build();

        savedBranch = Branch.builder()
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
    @DisplayName("Should create branch successfully with valid data")
    void shouldCreateBranchSuccessfully() {
        when(branchRepository.existsByName(anyString())).thenReturn(false);
        when(branchRepository.save(any(Branch.class))).thenReturn(savedBranch);
        when(branchDtoMapper.toResponse(any(Branch.class))).thenReturn(expectedResponse);

        BranchResponse result = createBranchUseCase.execute(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Branch");
        assertThat(result.getCapacity2r()).isEqualTo(50);
        assertThat(result.getCapacity4r()).isEqualTo(100);

        verify(branchRepository).existsByName("Test Branch");
        verify(branchRepository).save(any(Branch.class));
        verify(branchDtoMapper).toResponse(any(Branch.class));
    }

    @Test
    @DisplayName("Should throw DuplicateBranchNameException when branch name already exists")
    void shouldThrowExceptionWhenBranchNameExists() {
        when(branchRepository.existsByName(anyString())).thenReturn(true);

        assertThatThrownBy(() -> createBranchUseCase.execute(validRequest))
                .isInstanceOf(DuplicateBranchNameException.class)
                .hasMessageContaining("Branch with name 'Test Branch' already exists");

        verify(branchRepository).existsByName("Test Branch");
        verify(branchRepository, never()).save(any(Branch.class));
        verify(branchDtoMapper, never()).toResponse(any(Branch.class));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when closing time is before opening time")
    void shouldThrowExceptionWhenClosingTimeBeforeOpeningTime() {
        CreateBranchRequest invalidRequest = CreateBranchRequest.builder()
                .name("Test Branch")
                .address("123 Test Street, Test City")
                .openingTime("20:00")
                .closingTime("08:00")
                .capacity2r(50)
                .capacity4r(100)
                .build();

        when(branchRepository.existsByName(anyString())).thenReturn(false);

        assertThatThrownBy(() -> createBranchUseCase.execute(invalidRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Closing time must be after opening time");

        verify(branchRepository).existsByName("Test Branch");
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when opening time equals closing time")
    void shouldThrowExceptionWhenOpeningTimeEqualsClosingTime() {
        CreateBranchRequest invalidRequest = CreateBranchRequest.builder()
                .name("Test Branch")
                .address("123 Test Street, Test City")
                .openingTime("10:00")
                .closingTime("10:00")
                .capacity2r(50)
                .capacity4r(100)
                .build();

        when(branchRepository.existsByName(anyString())).thenReturn(false);

        assertThatThrownBy(() -> createBranchUseCase.execute(invalidRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Closing time must be after opening time");

        verify(branchRepository).existsByName("Test Branch");
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    @DisplayName("Should create branch with null rate per hour")
    void shouldCreateBranchWithNullRate() {
        CreateBranchRequest requestWithNullRate = CreateBranchRequest.builder()
                .name("Test Branch")
                .address("123 Test Street, Test City")
                .openingTime("08:00")
                .closingTime("20:00")
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(null)
                .build();

        Branch branchWithNullRate = Branch.builder()
                .id(1L)
                .name("Test Branch")
                .address("123 Test Street, Test City")
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(20, 0))
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(null)
                .isActive(true)
                .build();

        BranchResponse responseWithNullRate = BranchResponse.builder()
                .id(1L)
                .name("Test Branch")
                .ratePerHour(null)
                .build();

        when(branchRepository.existsByName(anyString())).thenReturn(false);
        when(branchRepository.save(any(Branch.class))).thenReturn(branchWithNullRate);
        when(branchDtoMapper.toResponse(any(Branch.class))).thenReturn(responseWithNullRate);

        BranchResponse result = createBranchUseCase.execute(requestWithNullRate);

        assertThat(result).isNotNull();
        assertThat(result.getRatePerHour()).isNull();

        verify(branchRepository).save(any(Branch.class));
    }
}
