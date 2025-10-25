package com.ayd.parkcontrol.application.dto.response.rate;

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
public class RateBranchResponse {

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("rate_per_hour")
    private BigDecimal ratePerHour;

    @JsonProperty("is_active")
    private Boolean isActive;
}
