package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.response.branch.BranchResponse;
import com.ayd.parkcontrol.application.dto.response.common.PageResponse;
import com.ayd.parkcontrol.application.mapper.BranchDtoMapper;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListBranchesUseCase Tests")
class ListBranchesUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchDtoMapper branchDtoMapper;

    @InjectMocks
    private ListBranchesUseCase listBranchesUseCase;

    private List<Branch> branches;
    private List<BranchResponse> branchResponses;

    @BeforeEach
    void setUp() {
        branches = Arrays.asList(
                Branch.builder()
                        .id(1L)
                        .name("Branch 1")
                        .address("Address 1")
                        .openingTime(LocalTime.of(8, 0))
                        .closingTime(LocalTime.of(20, 0))
                        .capacity2r(50)
                        .capacity4r(100)
                        .ratePerHour(new BigDecimal("15.00"))
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                Branch.builder()
                        .id(2L)
                        .name("Branch 2")
                        .address("Address 2")
                        .openingTime(LocalTime.of(7, 0))
                        .closingTime(LocalTime.of(22, 0))
                        .capacity2r(30)
                        .capacity4r(80)
                        .ratePerHour(new BigDecimal("20.00"))
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());

        branchResponses = Arrays.asList(
                BranchResponse.builder().id(1L).name("Branch 1").build(),
                BranchResponse.builder().id(2L).name("Branch 2").build());
    }

    @Test
    @DisplayName("Should return paginated branches successfully")
    void shouldReturnPaginatedBranchesSuccessfully() {
        Page<Branch> branchPage = new PageImpl<>(branches, PageRequest.of(0, 10), branches.size());

        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branchPage);
        when(branchDtoMapper.toResponse(any(Branch.class)))
                .thenReturn(branchResponses.get(0), branchResponses.get(1));

        PageResponse<BranchResponse> result = listBranchesUseCase.execute(0, 10, "name", "asc", null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPage_number()).isEqualTo(0);
        assertThat(result.getPage_size()).isEqualTo(10);
        assertThat(result.getTotal_elements()).isEqualTo(2L);
        assertThat(result.getIs_first()).isTrue();
        assertThat(result.getIs_last()).isTrue();

        verify(branchRepository).findAll(any(Pageable.class));
        verify(branchDtoMapper, times(2)).toResponse(any(Branch.class));
    }

    @Test
    @DisplayName("Should filter by active status")
    void shouldFilterByActiveStatus() {
        Page<Branch> branchPage = new PageImpl<>(branches, PageRequest.of(0, 10), branches.size());

        when(branchRepository.findByIsActive(anyBoolean(), any(Pageable.class))).thenReturn(branchPage);
        when(branchDtoMapper.toResponse(any(Branch.class)))
                .thenReturn(branchResponses.get(0), branchResponses.get(1));

        PageResponse<BranchResponse> result = listBranchesUseCase.execute(0, 10, "name", "asc", true);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        verify(branchRepository).findByIsActive(eq(true), any(Pageable.class));
        verify(branchRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle empty result")
    void shouldHandleEmptyResult() {
        Page<Branch> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(branchRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<BranchResponse> result = listBranchesUseCase.execute(0, 10, "name", "asc", null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotal_elements()).isEqualTo(0L);

        verify(branchRepository).findAll(any(Pageable.class));
        verify(branchDtoMapper, never()).toResponse(any(Branch.class));
    }

    @Test
    @DisplayName("Should sort descending when specified")
    void shouldSortDescendingWhenSpecified() {
        Page<Branch> branchPage = new PageImpl<>(branches, PageRequest.of(0, 10), branches.size());

        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branchPage);
        when(branchDtoMapper.toResponse(any(Branch.class)))
                .thenReturn(branchResponses.get(0), branchResponses.get(1));

        listBranchesUseCase.execute(0, 10, "name", "desc", null);

        verify(branchRepository).findAll(any(Pageable.class));
    }
}
