package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.response.commerce.BenefitResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetCommerceBenefitsUseCase {

    private final JpaBranchBusinessRepository branchBusinessRepository;
    private final JpaAffiliatedBusinessRepository commerceRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaBenefitTypeRepository benefitTypeRepository;
    private final JpaSettlementPeriodTypeRepository settlementPeriodTypeRepository;

    @Transactional(readOnly = true)
    public List<BenefitResponse> execute(Long commerceId) {
        log.info("Fetching benefits for commerce: {}", commerceId);

        List<BranchBusinessEntity> benefits = branchBusinessRepository.findByBusinessIdAndIsActiveTrue(commerceId);

        return benefits.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BenefitResponse mapToResponse(BranchBusinessEntity entity) {
        AffiliatedBusinessEntity commerce = commerceRepository.findById(entity.getBusinessId()).orElse(null);
        BranchEntity branch = branchRepository.findById(entity.getBranchId()).orElse(null);
        BenefitTypeEntity benefitType = benefitTypeRepository.findById(entity.getBenefitTypeId()).orElse(null);
        SettlementPeriodTypeEntity settlementPeriod = settlementPeriodTypeRepository
                .findById(entity.getSettlementPeriodTypeId()).orElse(null);

        return BenefitResponse.builder()
                .id(entity.getId())
                .businessId(entity.getBusinessId())
                .businessName(commerce != null ? commerce.getName() : null)
                .branchId(entity.getBranchId())
                .branchName(branch != null ? branch.getName() : null)
                .benefitTypeCode(benefitType != null ? benefitType.getCode() : null)
                .benefitTypeName(benefitType != null ? benefitType.getName() : null)
                .settlementPeriodCode(settlementPeriod != null ? settlementPeriod.getCode() : null)
                .settlementPeriodName(settlementPeriod != null ? settlementPeriod.getName() : null)
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
