package com.ayd.parkcontrol.application.usecase.rate;

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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteBranchRateUseCaseTest {

    @Mock
    private JpaBranchRepository branchRepository;

    @InjectMocks
    private DeleteBranchRateUseCase useCase;

    private BranchEntity branchEntity;

    @BeforeEach
    void setUp() {
        branchEntity = BranchEntity.builder()
                .id(1L)
                .name("Sucursal Centro")
                .ratePerHour(new BigDecimal("8.00"))
                .isActive(true)
                .build();
    }

    @Test
    void execute_shouldSetRateToNull_whenBranchExists() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branchEntity));

        useCase.execute(1L);

        verify(branchRepository).findById(1L);
        verify(branchRepository).save(any(BranchEntity.class));
    }

    @Test
    void execute_shouldThrowException_whenBranchNotFound() {
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Branch not found");

        verify(branchRepository).findById(999L);
        verify(branchRepository, never()).save(any());
    }
}
