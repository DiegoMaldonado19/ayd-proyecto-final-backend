package com.ayd.parkcontrol.application.dto.response.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {

    @JsonProperty("total_branches")
    private Integer totalBranches;

    @JsonProperty("active_tickets")
    private Long activeTickets;

    @JsonProperty("active_subscriptions")
    private Long activeSubscriptions;

    @JsonProperty("total_vehicles_today")
    private Long totalVehiclesToday;

    @JsonProperty("revenue_today")
    private BigDecimal revenueToday;

    @JsonProperty("average_occupancy_percentage")
    private Double averageOccupancyPercentage;

    @JsonProperty("pending_incidents")
    private Long pendingIncidents;

    @JsonProperty("pending_plate_changes")
    private Long pendingPlateChanges;
}
