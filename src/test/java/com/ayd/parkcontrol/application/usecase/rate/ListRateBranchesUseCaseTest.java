package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.application.dto.response.rate.RateBranchResponse;
import com.ayd.parkcontrol.application.mapper.RateDtoMapper;
import com.ayd.parkcontrol.domain.model.rate.RateBranch;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListRateBranchesUseCaseTest {

    @Mock
    private JpaBranchRepository branchRepository;

    @Mock
    private RateDtoMapper mapper;

    @InjectMocks
    private ListRateBranchesUseCase useCase;

    private BranchEntity branch1;
    private BranchEntity branch2;
    private RateBranch domain1;
    private RateBranch domain2;
    private RateBranchResponse response1;
    private RateBranchResponse response2;

    @BeforeEach
    void setUp() {
        branch1 = BranchEntity.builder()
                .id(1L)
                .name("Sucursal Centro")
                .ratePerHour(new BigDecimal("8.00"))
                .isActive(true)
                .build();

        branch2 = BranchEntity.builder()
                .id(2L)
                .name("Sucursal Norte")
                .ratePerHour(null)
                .isActive(true)
                .build();

        domain1 = RateBranch.builder()
                .branchId(1L)
                .branchName("Sucursal Centro")
                .ratePerHour(new BigDecimal("8.00"))
                .build();

        domain2 = RateBranch.builder()
                .branchId(2L)
                .branchName("Sucursal Norte")
                .ratePerHour(null)
                .build();

        response1 = RateBranchResponse.builder()
                .branchId(1L)
                .branchName("Sucursal Centro")
                .ratePerHour(new BigDecimal("8.00"))
                .build();

        response2 = RateBranchResponse.builder()
                .branchId(2L)
                .branchName("Sucursal Norte")
                .ratePerHour(null)
                .build();
    }

    @Test
    void execute_shouldReturnAllBranchesWithTheirRates() {
        List<BranchEntity> branches = Arrays.asList(branch1, branch2);
        when(branchRepository.findAll()).thenReturn(branches);
        when(mapper.toDomain(branch1)).thenReturn(domain1);
        when(mapper.toDomain(branch2)).thenReturn(domain2);
        when(mapper.toRateBranchResponse(domain1)).thenReturn(response1);
        when(mapper.toRateBranchResponse(domain2)).thenReturn(response2);

        List<RateBranchResponse> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBranchId()).isEqualTo(1L);
        assertThat(result.get(0).getRatePerHour()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(result.get(1).getBranchId()).isEqualTo(2L);
        assertThat(result.get(1).getRatePerHour()).isNull();

        verify(branchRepository).findAll();
        verify(mapper, times(2)).toDomain(any(BranchEntity.class));
        verify(mapper, times(2)).toRateBranchResponse(any(RateBranch.class));
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoBranchesExist() {
        when(branchRepository.findAll()).thenReturn(List.of());

        List<RateBranchResponse> result = useCase.execute();

        assertThat(result).isEmpty();
        verify(branchRepository).findAll();
        verifyNoMoreInteractions(mapper);
    }
}
