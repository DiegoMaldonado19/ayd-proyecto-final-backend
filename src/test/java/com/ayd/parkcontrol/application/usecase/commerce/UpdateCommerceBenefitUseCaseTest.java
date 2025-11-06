package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.UpdateCommerceBenefitRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.BenefitResponse;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCommerceBenefitUseCaseTest {

    @Mock
    private JpaBranchBusinessRepository branchBusinessRepository;

    @Mock
    private JpaAffiliatedBusinessRepository commerceRepository;

    @Mock
    private JpaBranchRepository branchRepository;

    @Mock
    private JpaBenefitTypeRepository benefitTypeRepository;

    @Mock
    private JpaSettlementPeriodTypeRepository settlementPeriodTypeRepository;

    @InjectMocks
    private UpdateCommerceBenefitUseCase useCase;

    private UpdateCommerceBenefitRequest request;
    private AffiliatedBusinessEntity commerce;
    private BranchBusinessEntity branchBusiness;
    private BranchEntity branch;
    private BenefitTypeEntity benefitType;
    private SettlementPeriodTypeEntity settlementPeriod;

    @BeforeEach
    void setUp() {
        request = UpdateCommerceBenefitRequest.builder()
                .benefitType("DESCUENTO")
                .settlementPeriod("MENSUAL")
                .build();

        commerce = AffiliatedBusinessEntity.builder()
                .id(1L)
                .name("Commerce Test")
                .build();

        branchBusiness = BranchBusinessEntity.builder()
                .id(10L)
                .businessId(1L)
                .branchId(5L)
                .benefitTypeId(1)
                .settlementPeriodTypeId(1)
                .isActive(true)
                .build();

        branch = BranchEntity.builder()
                .id(5L)
                .name("Branch Test")
                .build();

        benefitType = BenefitTypeEntity.builder()
                .id(2)
                .code("DESCUENTO")
                .name("Descuento")
                .build();

        settlementPeriod = SettlementPeriodTypeEntity.builder()
                .id(2)
                .code("MENSUAL")
                .name("Mensual")
                .build();
    }

    @Test
    void execute_shouldUpdateBenefit() {
        // Given
        when(commerceRepository.findById(1L)).thenReturn(Optional.of(commerce));
        when(branchBusinessRepository.findById(10L)).thenReturn(Optional.of(branchBusiness));
        when(branchRepository.findById(5L)).thenReturn(Optional.of(branch));
        when(benefitTypeRepository.findByCode("DESCUENTO")).thenReturn(Optional.of(benefitType));
        when(settlementPeriodTypeRepository.findByCode("MENSUAL")).thenReturn(Optional.of(settlementPeriod));
        when(branchBusinessRepository.save(any(BranchBusinessEntity.class))).thenReturn(branchBusiness);

        // When
        BenefitResponse result = useCase.execute(1L, 10L, request);

        // Then
        assertThat(result).isNotNull();
        verify(branchBusinessRepository, times(1)).findById(10L);
        verify(branchBusinessRepository, times(1)).save(any(BranchBusinessEntity.class));
    }

    @Test
    void execute_shouldThrowNotFoundException_whenCommerceNotFound() {
        // Given
        when(commerceRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(1L, 10L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Commerce not found");

        verify(branchBusinessRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowNotFoundException_whenBenefitNotFound() {
        // Given
        when(commerceRepository.findById(1L)).thenReturn(Optional.of(commerce));
        when(branchBusinessRepository.findById(10L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(1L, 10L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Benefit not found");

        verify(branchBusinessRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowNotFoundException_whenBenefitDoesNotBelongToCommerce() {
        // Given
        branchBusiness.setBusinessId(999L); // Different commerce
        when(commerceRepository.findById(1L)).thenReturn(Optional.of(commerce));
        when(branchBusinessRepository.findById(10L)).thenReturn(Optional.of(branchBusiness));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(1L, 10L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("does not belong to commerce");

        verify(branchBusinessRepository, never()).save(any());
    }
}
