package com.ayd.parkcontrol.application.dto.response.branch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyResponse {

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("capacity_2r")
    private Integer capacity2r;

    @JsonProperty("occupied_2r")
    private Integer occupied2r;

    @JsonProperty("available_2r")
    private Integer available2r;

    @JsonProperty("occupancy_percentage_2r")
    private Double occupancyPercentage2r;

    @JsonProperty("capacity_4r")
    private Integer capacity4r;

    @JsonProperty("occupied_4r")
    private Integer occupied4r;

    @JsonProperty("available_4r")
    private Integer available4r;

    @JsonProperty("occupancy_percentage_4r")
    private Double occupancyPercentage4r;

    @JsonProperty("total_capacity")
    private Integer totalCapacity;

    @JsonProperty("total_occupied")
    private Integer totalOccupied;

    @JsonProperty("total_available")
    private Integer totalAvailable;

    @JsonProperty("total_occupancy_percentage")
    private Double totalOccupancyPercentage;

    @JsonProperty("timestamp")
    private String timestamp;
}
