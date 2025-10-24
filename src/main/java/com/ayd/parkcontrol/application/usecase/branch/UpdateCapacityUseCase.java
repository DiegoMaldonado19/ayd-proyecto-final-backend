package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.request.branch.UpdateCapacityRequest;
import com.ayd.parkcontrol.application.dto.response.branch.BranchCapacityResponse;
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
public class UpdateCapacityUseCase {

    private final BranchRepository branchRepository;
    private final BranchDtoMapper branchDtoMapper;

    @Transactional
    public BranchCapacityResponse execute(Long branchId, UpdateCapacityRequest request) {
        log.debug("Updating capacity for branch with ID: {}", branchId);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch with ID " + branchId + " not found"));

        branch.setCapacity2r(request.getCapacity2r());
        branch.setCapacity4r(request.getCapacity4r());

        Branch updatedBranch = branchRepository.save(branch);

        log.info("Capacity updated for branch with ID {}: 2R={}, 4R={}",
                branchId, request.getCapacity2r(), request.getCapacity4r());

        return branchDtoMapper.toCapacityResponse(updatedBranch);
    }
}
