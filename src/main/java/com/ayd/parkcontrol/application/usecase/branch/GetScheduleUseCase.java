package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.response.branch.BranchScheduleResponse;
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
public class GetScheduleUseCase {

    private final BranchRepository branchRepository;
    private final BranchDtoMapper branchDtoMapper;

    @Transactional(readOnly = true)
    public BranchScheduleResponse execute(Long branchId) {
        log.debug("Fetching schedule for branch with ID: {}", branchId);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch with ID " + branchId + " not found"));

        return branchDtoMapper.toScheduleResponse(branch);
    }
}
