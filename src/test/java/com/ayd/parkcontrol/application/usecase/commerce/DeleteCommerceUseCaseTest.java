package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AffiliatedBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAffiliatedBusinessRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchBusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCommerceUseCaseTest {

    @Mock
    private JpaAffiliatedBusinessRepository commerceRepository;

    @Mock
    private JpaBranchBusinessRepository branchBusinessRepository;

    @InjectMocks
    private DeleteCommerceUseCase deleteCommerceUseCase;

    private AffiliatedBusinessEntity commerce;

    @BeforeEach
    void setUp() {
        commerce = AffiliatedBusinessEntity.builder()
                .id(1L)
                .name("Test Commerce")
                .taxId("12345678")
                .email("test@commerce.com")
                .phone("12345678")
                .ratePerHour(BigDecimal.valueOf(10.00))
                .isActive(true)
                .build();
    }

    @Test
    void execute_shouldDeleteCommerce_whenNoActiveBenefits() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(branchBusinessRepository.findByBusinessIdAndIsActiveTrue(anyLong())).thenReturn(List.of());
        doNothing().when(commerceRepository).delete(any(AffiliatedBusinessEntity.class));

        deleteCommerceUseCase.execute(1L);

        verify(commerceRepository).delete(commerce);
    }

    @Test
    void execute_shouldThrowNotFoundException_whenCommerceDoesNotExist() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteCommerceUseCase.execute(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Commerce not found with ID: 999");

        verify(commerceRepository, never()).delete(any());
    }

    @Test
    void execute_shouldThrowBusinessRuleException_whenCommerceHasActiveBenefits() {
        BranchBusinessEntity activeBenefit = BranchBusinessEntity.builder()
                .id(1L)
                .businessId(1L)
                .branchId(1L)
                .benefitTypeId(1)
                .isActive(true)
                .build();

        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(branchBusinessRepository.findByBusinessIdAndIsActiveTrue(anyLong()))
                .thenReturn(List.of(activeBenefit));

        assertThatThrownBy(() -> deleteCommerceUseCase.execute(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot delete commerce with active benefits");

        verify(commerceRepository, never()).delete(any());
    }

    @Test
    void execute_shouldDeleteCommerce_whenBenefitsAreInactive() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(branchBusinessRepository.findByBusinessIdAndIsActiveTrue(anyLong())).thenReturn(List.of());
        doNothing().when(commerceRepository).delete(any(AffiliatedBusinessEntity.class));

        deleteCommerceUseCase.execute(1L);

        verify(commerceRepository).delete(commerce);
    }
}
