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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRateBranchUseCaseTest {

    @Mock
    private JpaBranchRepository branchRepository;

    @Mock
    private RateDtoMapper mapper;

    @InjectMocks
    private GetRateBranchUseCase useCase;

    private BranchEntity branchEntity;
    private RateBranch rateBranch;
    private RateBranchResponse response;

    @BeforeEach
    void setUp() {
        branchEntity = BranchEntity.builder()
                .id(1L)
                .name("Sucursal Centro")
                .ratePerHour(new BigDecimal("8.00"))
                .isActive(true)
                .build();

        rateBranch = RateBranch.builder()
                .branchId(1L)
                .branchName("Sucursal Centro")
                .ratePerHour(new BigDecimal("8.00"))
                .build();

        response = RateBranchResponse.builder()
                .branchId(1L)
                .branchName("Sucursal Centro")
                .ratePerHour(new BigDecimal("8.00"))
                .build();
    }

    @Test
    void execute_shouldReturnBranchRate_whenBranchExists() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branchEntity));
        when(mapper.toDomain(branchEntity)).thenReturn(rateBranch);
        when(mapper.toRateBranchResponse(rateBranch)).thenReturn(response);

        RateBranchResponse result = useCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getBranchId()).isEqualTo(1L);
        assertThat(result.getBranchName()).isEqualTo("Sucursal Centro");
        assertThat(result.getRatePerHour()).isEqualByComparingTo(new BigDecimal("8.00"));

        verify(branchRepository).findById(1L);
        verify(mapper).toDomain(branchEntity);
        verify(mapper).toRateBranchResponse(rateBranch);
    }

    @Test
    void execute_shouldThrowException_whenBranchNotFound() {
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Branch not found");

        verify(branchRepository).findById(999L);
        verifyNoInteractions(mapper);
    }
}
