package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchBusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCommerceBenefitUseCaseTest {

    @Mock
    private JpaBranchBusinessRepository branchBusinessRepository;

    @InjectMocks
    private DeleteCommerceBenefitUseCase useCase;

    private BranchBusinessEntity branchBusiness;

    @BeforeEach
    void setUp() {
        branchBusiness = BranchBusinessEntity.builder()
                .id(10L)
                .businessId(1L)
                .branchId(5L)
                .benefitTypeId(1)
                .settlementPeriodTypeId(1)
                .isActive(true)
                .build();
    }

    @Test
    void execute_shouldDeleteBenefit() {
        // Given
        when(branchBusinessRepository.findById(10L)).thenReturn(Optional.of(branchBusiness));

        // When
        useCase.execute(1L, 10L);

        // Then
        verify(branchBusinessRepository, times(1)).findById(10L);
        verify(branchBusinessRepository, times(1)).delete(branchBusiness);
    }

    @Test
    void execute_shouldThrowNotFoundException_whenBenefitNotFound() {
        // Given
        when(branchBusinessRepository.findById(10L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(1L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Benefit not found");

        verify(branchBusinessRepository, never()).delete(any());
    }

    @Test
    void execute_shouldThrowNotFoundException_whenBenefitDoesNotBelongToCommerce() {
        // Given
        branchBusiness.setBusinessId(999L); // Different commerce
        when(branchBusinessRepository.findById(10L)).thenReturn(Optional.of(branchBusiness));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(1L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("does not belong to commerce");

        verify(branchBusinessRepository, never()).delete(any());
    }
}
