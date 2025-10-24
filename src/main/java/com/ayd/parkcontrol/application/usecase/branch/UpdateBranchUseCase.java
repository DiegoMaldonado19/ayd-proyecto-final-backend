package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.request.branch.UpdateBranchRequest;
import com.ayd.parkcontrol.application.dto.response.branch.BranchResponse;
import com.ayd.parkcontrol.application.mapper.BranchDtoMapper;
import com.ayd.parkcontrol.domain.exception.BranchNotFoundException;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.DuplicateBranchNameException;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateBranchUseCase {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final BranchRepository branchRepository;
    private final BranchDtoMapper branchDtoMapper;

    @Transactional
    public BranchResponse execute(Long branchId, UpdateBranchRequest request) {
        log.debug("Updating branch with ID: {}", branchId);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch with ID " + branchId + " not found"));

        updateBranchFields(branch, request);

        Branch updatedBranch = branchRepository.save(branch);

        log.info("Branch with ID {} updated successfully", branchId);

        return branchDtoMapper.toResponse(updatedBranch);
    }

    private void updateBranchFields(Branch branch, UpdateBranchRequest request) {
        if (request.getName() != null && !request.getName().equals(branch.getName())) {
            validateBranchNameUniqueness(request.getName(), branch.getId());
            branch.setName(request.getName());
        }

        if (request.getAddress() != null) {
            branch.setAddress(request.getAddress());
        }

        if (request.getOpeningTime() != null || request.getClosingTime() != null) {
            LocalTime newOpening = request.getOpeningTime() != null
                    ? LocalTime.parse(request.getOpeningTime(), TIME_FORMATTER)
                    : branch.getOpeningTime();
            LocalTime newClosing = request.getClosingTime() != null
                    ? LocalTime.parse(request.getClosingTime(), TIME_FORMATTER)
                    : branch.getClosingTime();

            validateSchedule(newOpening, newClosing);

            if (request.getOpeningTime() != null) {
                branch.setOpeningTime(newOpening);
            }
            if (request.getClosingTime() != null) {
                branch.setClosingTime(newClosing);
            }
        }

        if (request.getRatePerHour() != null) {
            branch.setRatePerHour(request.getRatePerHour());
        }

        if (request.getIsActive() != null) {
            branch.setIsActive(request.getIsActive());
        }
    }

    private void validateBranchNameUniqueness(String name, Long branchId) {
        if (branchRepository.existsByNameAndIdNot(name, branchId)) {
            throw new DuplicateBranchNameException("Branch with name '" + name + "' already exists");
        }
    }

    private void validateSchedule(LocalTime openingTime, LocalTime closingTime) {
        if (!closingTime.isAfter(openingTime)) {
            throw new BusinessRuleException("Closing time must be after opening time");
        }
    }
}
