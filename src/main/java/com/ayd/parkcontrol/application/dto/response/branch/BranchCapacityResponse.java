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
public class BranchCapacityResponse {

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("capacity_2r")
    private Integer capacity2r;

    @JsonProperty("capacity_4r")
    private Integer capacity4r;

    @JsonProperty("total_capacity")
    private Integer totalCapacity;
}
