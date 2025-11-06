package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchBusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCommerceBenefitUseCase {

    private final JpaBranchBusinessRepository branchBusinessRepository;

    @Transactional
    public void execute(Long commerceId, Long benefitId) {
        log.info("Deleting benefit {} for commerce {}", benefitId, commerceId);

        // Verify benefit exists and belongs to this commerce
        BranchBusinessEntity branchBusiness = branchBusinessRepository.findById(benefitId)
                .orElseThrow(() -> new NotFoundException("Benefit not found with ID: " + benefitId));

        if (!branchBusiness.getBusinessId().equals(commerceId)) {
            throw new NotFoundException("Benefit " + benefitId + " does not belong to commerce " + commerceId);
        }

        branchBusinessRepository.delete(branchBusiness);

        log.info("Benefit {} deleted successfully", benefitId);
    }
}
