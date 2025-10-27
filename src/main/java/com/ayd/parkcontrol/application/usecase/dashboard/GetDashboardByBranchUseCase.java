package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.DashboardOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GetDashboardByBranchUseCase {

    @Transactional(readOnly = true)
    public DashboardOverviewResponse execute(Long branchId) {
        return DashboardOverviewResponse.builder()
                .totalBranches(1)
                .activeTickets(0L)
                .activeSubscriptions(0L)
                .totalVehiclesToday(0L)
                .revenueToday(BigDecimal.ZERO)
                .averageOccupancyPercentage(0.0)
                .pendingIncidents(0L)
                .pendingPlateChanges(0L)
                .build();
    }
}
