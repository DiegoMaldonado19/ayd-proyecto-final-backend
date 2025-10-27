package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.DashboardOverviewResponse;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GetDashboardOverviewUseCase {

    private final BranchRepository branchRepository;
    private final PlateChangeRequestRepository plateChangeRequestRepository;

    @Transactional(readOnly = true)
    public DashboardOverviewResponse execute() {
        long totalBranches = branchRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
        long pendingPlateChanges = plateChangeRequestRepository.findAll().size();

        return DashboardOverviewResponse.builder()
                .totalBranches((int) totalBranches)
                .activeTickets(0L)
                .activeSubscriptions(0L)
                .totalVehiclesToday(0L)
                .revenueToday(BigDecimal.ZERO)
                .averageOccupancyPercentage(0.0)
                .pendingIncidents(0L)
                .pendingPlateChanges((long) pendingPlateChanges)
                .build();
    }
}
