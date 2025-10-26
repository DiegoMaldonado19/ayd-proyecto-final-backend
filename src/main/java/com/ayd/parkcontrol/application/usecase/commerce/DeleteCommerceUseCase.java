package com.ayd.parkcontrol.application.usecase.commerce;

import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AffiliatedBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAffiliatedBusinessRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchBusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCommerceUseCase {

    private final JpaAffiliatedBusinessRepository commerceRepository;
    private final JpaBranchBusinessRepository branchBusinessRepository;

    @Transactional
    public void execute(Long id) {
        log.info("Deleting commerce with ID: {}", id);

        AffiliatedBusinessEntity commerce = commerceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commerce not found with ID: " + id));

        long activeBenefits = branchBusinessRepository.findByBusinessIdAndIsActiveTrue(id).stream()
                .filter(bb -> bb.getIsActive())
                .count();

        if (activeBenefits > 0) {
            throw new BusinessRuleException(
                    "Cannot delete commerce with active benefits. Deactivate all benefits first.");
        }

        commerceRepository.delete(commerce);

        log.info("Commerce deleted successfully: {}", commerce.getName());
    }
}
