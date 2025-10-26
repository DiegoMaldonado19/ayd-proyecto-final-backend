package com.ayd.parkcontrol.application.dto.request.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigureBenefitRequest {

    @NotNull(message = "Branch ID is required")
    @JsonProperty("branch_id")
    private Long branchId;

    @NotBlank(message = "Benefit type is required")
    @JsonProperty("benefit_type")
    private String benefitType;

    @NotBlank(message = "Settlement period is required")
    @JsonProperty("settlement_period")
    private String settlementPeriod;
}
