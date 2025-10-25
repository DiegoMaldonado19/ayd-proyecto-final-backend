package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.application.dto.request.rate.UpdateBranchRateRequest;
import com.ayd.parkcontrol.application.dto.response.rate.RateBranchResponse;
import com.ayd.parkcontrol.application.mapper.RateDtoMapper;
import com.ayd.parkcontrol.domain.model.rate.RateBranch;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateBranchRateUseCase {

    private final JpaBranchRepository branchRepository;
    private final RateDtoMapper mapper;

    @Transactional
    public RateBranchResponse execute(Long branchId, UpdateBranchRateRequest request) {
        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + branchId));

        branch.setRatePerHour(request.getRatePerHour());
        BranchEntity updatedBranch = branchRepository.save(branch);

        RateBranch domain = mapper.toDomain(updatedBranch);
        return mapper.toRateBranchResponse(domain);
    }
}
