package com.ayd.parkcontrol.application.dto.response.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyDetailResponse {

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

    @JsonProperty("available_spaces")
    private Integer availableSpaces;

    @JsonProperty("occupancy_percentage")
    private Double occupancyPercentage;
}
