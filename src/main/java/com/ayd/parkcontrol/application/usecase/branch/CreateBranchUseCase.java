package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.request.branch.CreateBranchRequest;
import com.ayd.parkcontrol.application.dto.response.branch.BranchResponse;
import com.ayd.parkcontrol.application.mapper.BranchDtoMapper;
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
public class CreateBranchUseCase {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final BranchRepository branchRepository;
    private final BranchDtoMapper branchDtoMapper;

    @Transactional
    public BranchResponse execute(CreateBranchRequest request) {
        log.debug("Creating branch with name: {}", request.getName());

        validateBranchNameUniqueness(request.getName());
        validateSchedule(request.getOpeningTime(), request.getClosingTime());

        Branch branch = buildBranchFromRequest(request);
        Branch savedBranch = branchRepository.save(branch);

        log.info("Branch created successfully with ID: {}", savedBranch.getId());

        return branchDtoMapper.toResponse(savedBranch);
    }

    private void validateBranchNameUniqueness(String name) {
        if (branchRepository.existsByName(name)) {
            throw new DuplicateBranchNameException("Branch with name '" + name + "' already exists");
        }
    }

    private void validateSchedule(String openingTime, String closingTime) {
        LocalTime opening = LocalTime.parse(openingTime, TIME_FORMATTER);
        LocalTime closing = LocalTime.parse(closingTime, TIME_FORMATTER);

        if (!closing.isAfter(opening)) {
            throw new BusinessRuleException("Closing time must be after opening time");
        }
    }

    private Branch buildBranchFromRequest(CreateBranchRequest request) {
        return Branch.builder()
                .name(request.getName())
                .address(request.getAddress())
                .openingTime(LocalTime.parse(request.getOpeningTime(), TIME_FORMATTER))
                .closingTime(LocalTime.parse(request.getClosingTime(), TIME_FORMATTER))
                .capacity2r(request.getCapacity2r())
                .capacity4r(request.getCapacity4r())
                .ratePerHour(request.getRatePerHour())
                .isActive(true)
                .build();
    }
}
