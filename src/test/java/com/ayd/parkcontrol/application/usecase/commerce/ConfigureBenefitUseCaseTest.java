package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.ConfigureBenefitRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.BenefitResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigureBenefitUseCaseTest {

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
    private ConfigureBenefitUseCase configureBenefitUseCase;

    private AffiliatedBusinessEntity commerce;
    private BranchEntity branch;
    private BenefitTypeEntity benefitType;
    private SettlementPeriodTypeEntity settlementPeriod;
    private ConfigureBenefitRequest request;

    @BeforeEach
    void setUp() {
        commerce = AffiliatedBusinessEntity.builder()
                .id(1L)
                .name("Test Commerce")
                .taxId("12345678")
                .email("commerce@test.com")
                .phone("12345678")
                .ratePerHour(BigDecimal.valueOf(10.00))
                .isActive(true)
                .build();

        branch = BranchEntity.builder()
                .id(1L)
                .name("Central Branch")
                .build();

        benefitType = BenefitTypeEntity.builder()
                .id(1)
                .code("DISCOUNT_10")
                .name("10% Discount")
                .build();

        settlementPeriod = SettlementPeriodTypeEntity.builder()
                .id(1)
                .code("MONTHLY")
                .name("Monthly Settlement")
                .build();

        request = ConfigureBenefitRequest.builder()
                .branchId(1L)
                .benefitType("DISCOUNT_10")
                .settlementPeriod("MONTHLY")
                .build();
    }

    @Test
    void execute_shouldConfigureBenefitSuccessfully_whenDataIsValid() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
        when(benefitTypeRepository.findByCode(anyString())).thenReturn(Optional.of(benefitType));
        when(settlementPeriodTypeRepository.findByCode(anyString())).thenReturn(Optional.of(settlementPeriod));
        when(branchBusinessRepository.existsByBusinessIdAndBranchIdAndIsActiveTrue(anyLong(), anyLong()))
                .thenReturn(false);

        BranchBusinessEntity savedEntity = BranchBusinessEntity.builder()
                .id(1L)
                .businessId(1L)
                .branchId(1L)
                .benefitTypeId(1)
                .settlementPeriodTypeId(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(branchBusinessRepository.save(any(BranchBusinessEntity.class))).thenReturn(savedEntity);

        BenefitResponse response = configureBenefitUseCase.execute(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBusinessName()).isEqualTo("Test Commerce");
        assertThat(response.getBranchName()).isEqualTo("Central Branch");
        assertThat(response.getBenefitTypeCode()).isEqualTo("DISCOUNT_10");
        assertThat(response.getSettlementPeriodCode()).isEqualTo("MONTHLY");
        assertThat(response.getIsActive()).isTrue();

        verify(branchBusinessRepository).save(any(BranchBusinessEntity.class));
    }

    @Test
    void execute_shouldThrowNotFoundException_whenCommerceNotFound() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configureBenefitUseCase.execute(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Commerce not found");

        verify(branchBusinessRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowNotFoundException_whenBranchNotFound() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configureBenefitUseCase.execute(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Branch not found");

        verify(branchBusinessRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowNotFoundException_whenBenefitTypeNotFound() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
        when(benefitTypeRepository.findByCode(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configureBenefitUseCase.execute(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Benefit type not found");

        verify(branchBusinessRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowNotFoundException_whenSettlementPeriodNotFound() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
        when(benefitTypeRepository.findByCode(anyString())).thenReturn(Optional.of(benefitType));
        when(settlementPeriodTypeRepository.findByCode(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configureBenefitUseCase.execute(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Settlement period not found");

        verify(branchBusinessRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowBusinessRuleException_whenBenefitAlreadyExists() {
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
        when(benefitTypeRepository.findByCode(anyString())).thenReturn(Optional.of(benefitType));
        when(settlementPeriodTypeRepository.findByCode(anyString())).thenReturn(Optional.of(settlementPeriod));
        when(branchBusinessRepository.existsByBusinessIdAndBranchIdAndIsActiveTrue(anyLong(), anyLong()))
                .thenReturn(true);

        assertThatThrownBy(() -> configureBenefitUseCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Commerce already has an active benefit");

        verify(branchBusinessRepository, never()).save(any());
    }
}
