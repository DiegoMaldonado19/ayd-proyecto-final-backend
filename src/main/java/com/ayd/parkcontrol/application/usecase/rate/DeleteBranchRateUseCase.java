package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteBranchRateUseCase {

    private final JpaBranchRepository branchRepository;

    @Transactional
    public void execute(Long branchId) {
        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + branchId));

        branch.setRatePerHour(null);
        branchRepository.save(branch);
    }
}
