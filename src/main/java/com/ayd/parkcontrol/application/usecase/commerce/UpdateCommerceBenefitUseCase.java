package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.UpdateCommerceBenefitRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.BenefitResponse;
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
public class UpdateCommerceBenefitUseCase {

    private final JpaBranchBusinessRepository branchBusinessRepository;
    private final JpaAffiliatedBusinessRepository commerceRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaBenefitTypeRepository benefitTypeRepository;
    private final JpaSettlementPeriodTypeRepository settlementPeriodTypeRepository;

    @Transactional
    public BenefitResponse execute(Long commerceId, Long benefitId, UpdateCommerceBenefitRequest request) {
        log.info("Updating benefit {} for commerce {}", benefitId, commerceId);

        // Verify commerce exists
        AffiliatedBusinessEntity commerce = commerceRepository.findById(commerceId)
                .orElseThrow(() -> new NotFoundException("Commerce not found with ID: " + commerceId));

        // Verify benefit exists and belongs to this commerce
        BranchBusinessEntity branchBusiness = branchBusinessRepository.findById(benefitId)
                .orElseThrow(() -> new NotFoundException("Benefit not found with ID: " + benefitId));

        if (!branchBusiness.getBusinessId().equals(commerceId)) {
            throw new NotFoundException("Benefit " + benefitId + " does not belong to commerce " + commerceId);
        }

        // Get branch info
        BranchEntity branch = branchRepository.findById(branchBusiness.getBranchId())
                .orElseThrow(() -> new NotFoundException("Branch not found with ID: " + branchBusiness.getBranchId()));

        // Validate and get benefit type
        BenefitTypeEntity benefitType = benefitTypeRepository.findByCode(request.getBenefitType())
                .orElseThrow(() -> new NotFoundException("Benefit type not found: " + request.getBenefitType()));

        // Validate and get settlement period
        SettlementPeriodTypeEntity settlementPeriod = settlementPeriodTypeRepository
                .findByCode(request.getSettlementPeriod())
                .orElseThrow(
                        () -> new NotFoundException("Settlement period not found: " + request.getSettlementPeriod()));

        // Update benefit
        branchBusiness.setBenefitTypeId(benefitType.getId());
        branchBusiness.setSettlementPeriodTypeId(settlementPeriod.getId());

        BranchBusinessEntity updated = branchBusinessRepository.save(branchBusiness);

        log.info("Benefit {} updated successfully", benefitId);

        return mapToResponse(updated, commerce, branch, benefitType, settlementPeriod);
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
