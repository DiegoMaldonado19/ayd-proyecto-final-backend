package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.ConfigureBenefitRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.BenefitResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigureBenefitUseCase {

    private final JpaBranchBusinessRepository branchBusinessRepository;
    private final JpaAffiliatedBusinessRepository commerceRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaBenefitTypeRepository benefitTypeRepository;
    private final JpaSettlementPeriodTypeRepository settlementPeriodTypeRepository;

    @Transactional
    public BenefitResponse execute(Long commerceId, ConfigureBenefitRequest request) {
        log.info("Configuring benefit for commerce {} at branch {}", commerceId, request.getBranchId());

        AffiliatedBusinessEntity commerce = commerceRepository.findById(commerceId)
                .orElseThrow(() -> new NotFoundException("Commerce not found with ID: " + commerceId));

        BranchEntity branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new NotFoundException("Branch not found with ID: " + request.getBranchId()));

        BenefitTypeEntity benefitType = benefitTypeRepository.findByCode(request.getBenefitType())
                .orElseThrow(() -> new NotFoundException("Benefit type not found: " + request.getBenefitType()));

        SettlementPeriodTypeEntity settlementPeriod = settlementPeriodTypeRepository
                .findByCode(request.getSettlementPeriod())
                .orElseThrow(
                        () -> new NotFoundException("Settlement period not found: " + request.getSettlementPeriod()));

        if (branchBusinessRepository.existsByBusinessIdAndBranchIdAndIsActiveTrue(commerceId, request.getBranchId())) {
            throw new BusinessRuleException("Commerce already has an active benefit configured at this branch");
        }

        BranchBusinessEntity branchBusiness = BranchBusinessEntity.builder()
                .businessId(commerceId)
                .branchId(request.getBranchId())
                .benefitTypeId(benefitType.getId())
                .settlementPeriodTypeId(settlementPeriod.getId())
                .isActive(true)
                .build();

        BranchBusinessEntity saved = branchBusinessRepository.save(branchBusiness);

        log.info("Benefit configured successfully with ID: {}", saved.getId());

        return mapToResponse(saved, commerce, branch, benefitType, settlementPeriod);
    }

    private BenefitResponse mapToResponse(BranchBusinessEntity entity, AffiliatedBusinessEntity commerce,
            BranchEntity branch, BenefitTypeEntity benefitType,
            SettlementPeriodTypeEntity settlementPeriod) {
        return BenefitResponse.builder()
                .id(entity.getId())
                .businessId(entity.getBusinessId())
                .businessName(commerce.getName())
                .branchId(entity.getBranchId())
                .branchName(branch.getName())
                .benefitTypeCode(benefitType.getCode())
                .benefitTypeName(benefitType.getName())
                .settlementPeriodCode(settlementPeriod.getCode())
                .settlementPeriodName(settlementPeriod.getName())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
