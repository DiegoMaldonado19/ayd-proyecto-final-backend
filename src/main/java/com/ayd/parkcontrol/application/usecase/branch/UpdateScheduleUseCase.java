package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.request.branch.UpdateScheduleRequest;
import com.ayd.parkcontrol.application.dto.response.branch.BranchScheduleResponse;
import com.ayd.parkcontrol.application.mapper.BranchDtoMapper;
import com.ayd.parkcontrol.domain.exception.BranchNotFoundException;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
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
public class UpdateScheduleUseCase {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final BranchRepository branchRepository;
    private final BranchDtoMapper branchDtoMapper;

    @Transactional
    public BranchScheduleResponse execute(Long branchId, UpdateScheduleRequest request) {
        log.debug("Updating schedule for branch with ID: {}", branchId);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch with ID " + branchId + " not found"));

        LocalTime opening = LocalTime.parse(request.getOpeningTime(), TIME_FORMATTER);
        LocalTime closing = LocalTime.parse(request.getClosingTime(), TIME_FORMATTER);

        validateSchedule(opening, closing);

        branch.setOpeningTime(opening);
        branch.setClosingTime(closing);

        Branch updatedBranch = branchRepository.save(branch);

        log.info("Schedule updated for branch with ID {}: {}  - {}", branchId, opening, closing);

        return branchDtoMapper.toScheduleResponse(updatedBranch);
    }

    private void validateSchedule(LocalTime openingTime, LocalTime closingTime) {
        if (!closingTime.isAfter(openingTime)) {
            throw new BusinessRuleException("Closing time must be after opening time");
        }
    }
}
