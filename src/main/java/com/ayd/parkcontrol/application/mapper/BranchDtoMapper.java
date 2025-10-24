package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.branch.BranchCapacityResponse;
import com.ayd.parkcontrol.application.dto.response.branch.BranchResponse;
import com.ayd.parkcontrol.application.dto.response.branch.BranchScheduleResponse;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class BranchDtoMapper {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BranchResponse toResponse(Branch branch) {
        if (branch == null) {
            return null;
        }

        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .openingTime(branch.getOpeningTime() != null ? branch.getOpeningTime().format(TIME_FORMATTER) : null)
                .closingTime(branch.getClosingTime() != null ? branch.getClosingTime().format(TIME_FORMATTER) : null)
                .capacity2r(branch.getCapacity2r())
                .capacity4r(branch.getCapacity4r())
                .ratePerHour(branch.getRatePerHour())
                .isActive(branch.getIsActive())
                .createdAt(branch.getCreatedAt() != null ? branch.getCreatedAt().format(DATETIME_FORMATTER) : null)
                .updatedAt(branch.getUpdatedAt() != null ? branch.getUpdatedAt().format(DATETIME_FORMATTER) : null)
                .build();
    }

    public BranchCapacityResponse toCapacityResponse(Branch branch) {
        if (branch == null) {
            return null;
        }

        return BranchCapacityResponse.builder()
                .branchId(branch.getId())
                .branchName(branch.getName())
                .capacity2r(branch.getCapacity2r())
                .capacity4r(branch.getCapacity4r())
                .totalCapacity(branch.getCapacity2r() + branch.getCapacity4r())
                .build();
    }

    public BranchScheduleResponse toScheduleResponse(Branch branch) {
        if (branch == null) {
            return null;
        }

        LocalTime now = LocalTime.now();
        boolean isOpenNow = false;

        if (branch.getOpeningTime() != null && branch.getClosingTime() != null) {
            isOpenNow = !now.isBefore(branch.getOpeningTime()) && !now.isAfter(branch.getClosingTime());
        }

        return BranchScheduleResponse.builder()
                .branchId(branch.getId())
                .branchName(branch.getName())
                .openingTime(branch.getOpeningTime() != null ? branch.getOpeningTime().format(TIME_FORMATTER) : null)
                .closingTime(branch.getClosingTime() != null ? branch.getClosingTime().format(TIME_FORMATTER) : null)
                .isOpenNow(isOpenNow)
                .build();
    }
}
