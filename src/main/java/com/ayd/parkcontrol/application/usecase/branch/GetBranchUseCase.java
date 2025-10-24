package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.response.branch.BranchResponse;
import com.ayd.parkcontrol.application.mapper.BranchDtoMapper;
import com.ayd.parkcontrol.domain.exception.BranchNotFoundException;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetBranchUseCase {

    private final BranchRepository branchRepository;
    private final BranchDtoMapper branchDtoMapper;

    @Transactional(readOnly = true)
    public BranchResponse execute(Long branchId) {
        log.debug("Fetching branch with ID: {}", branchId);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch with ID " + branchId + " not found"));

        log.debug("Branch found: {}", branch.getName());

        return branchDtoMapper.toResponse(branch);
    }
}
