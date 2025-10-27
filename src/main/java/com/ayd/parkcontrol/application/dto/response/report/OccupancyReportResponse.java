package com.ayd.parkcontrol.application.dto.response.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyReportResponse {

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("vehicle_type")
    private String vehicleType;

    @JsonProperty("total_capacity")
    private Integer totalCapacity;

    @JsonProperty("current_occupancy")
    private Integer currentOccupancy;

    @JsonProperty("peak_occupancy")
    private Integer peakOccupancy;

    @JsonProperty("average_occupancy")
    private Double averageOccupancy;

    @JsonProperty("occupancy_percentage")
    private Double occupancyPercentage;
}
