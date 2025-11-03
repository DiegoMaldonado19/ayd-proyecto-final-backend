package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.response.commerce.BenefitResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCommerceBenefitsUseCaseTest {

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
    private GetCommerceBenefitsUseCase getCommerceBenefitsUseCase;

    private AffiliatedBusinessEntity commerce;
    private BranchEntity branch;
    private BenefitTypeEntity benefitType;
    private SettlementPeriodTypeEntity settlementPeriod;
    private BranchBusinessEntity benefit;

    @BeforeEach
    void setUp() {
        commerce = AffiliatedBusinessEntity.builder()
                .id(1L)
                .name("Test Commerce")
                .build();

        branch = BranchEntity.builder()
                .id(1L)
                .name("Test Branch")
                .build();

        benefitType = BenefitTypeEntity.builder()
                .id(1)
                .code("DISCOUNT")
                .name("Discount Benefit")
                .build();

        settlementPeriod = SettlementPeriodTypeEntity.builder()
                .id(1)
                .code("MONTHLY")
                .name("Monthly Settlement")
                .build();

        benefit = BranchBusinessEntity.builder()
                .id(1L)
                .businessId(1L)
                .branchId(1L)
                .benefitTypeId(1)
                .settlementPeriodTypeId(1)
                .isActive(true)
                .build();
    }

    @Test
    void execute_shouldReturnBenefits_whenCommerceBenefitsExist() {
        when(branchBusinessRepository.findByBusinessIdAndIsActiveTrue(anyLong())).thenReturn(List.of(benefit));
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.of(commerce));
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
        when(benefitTypeRepository.findById(anyInt())).thenReturn(Optional.of(benefitType));
        when(settlementPeriodTypeRepository.findById(anyInt())).thenReturn(Optional.of(settlementPeriod));

        List<BenefitResponse> result = getCommerceBenefitsUseCase.execute(1L);

        assertThat(result).hasSize(1);
        BenefitResponse response = result.get(0);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBusinessId()).isEqualTo(1L);
        assertThat(response.getBusinessName()).isEqualTo("Test Commerce");
        assertThat(response.getBranchId()).isEqualTo(1L);
        assertThat(response.getBranchName()).isEqualTo("Test Branch");
        assertThat(response.getBenefitTypeCode()).isEqualTo("DISCOUNT");
        assertThat(response.getBenefitTypeName()).isEqualTo("Discount Benefit");
        assertThat(response.getSettlementPeriodCode()).isEqualTo("MONTHLY");
        assertThat(response.getSettlementPeriodName()).isEqualTo("Monthly Settlement");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void execute_shouldReturnEmptyList_whenCommerceBenefitsDoNotExist() {
        when(branchBusinessRepository.findByBusinessIdAndIsActiveTrue(anyLong())).thenReturn(List.of());

        List<BenefitResponse> result = getCommerceBenefitsUseCase.execute(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void execute_shouldHandleNullReferences_whenRelatedEntitiesNotFound() {
        when(branchBusinessRepository.findByBusinessIdAndIsActiveTrue(anyLong())).thenReturn(List.of(benefit));
        when(commerceRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(benefitTypeRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(settlementPeriodTypeRepository.findById(anyInt())).thenReturn(Optional.empty());

        List<BenefitResponse> result = getCommerceBenefitsUseCase.execute(1L);

        assertThat(result).hasSize(1);
        BenefitResponse response = result.get(0);
        assertThat(response.getBusinessName()).isNull();
        assertThat(response.getBranchName()).isNull();
        assertThat(response.getBenefitTypeCode()).isNull();
        assertThat(response.getBenefitTypeName()).isNull();
        assertThat(response.getSettlementPeriodCode()).isNull();
        assertThat(response.getSettlementPeriodName()).isNull();
    }
}
