package com.ayd.parkcontrol.application.dto.request.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommerceBenefitRequest {

    @NotBlank(message = "Benefit type is required")
    @JsonProperty("benefit_type")
    private String benefitType;

    @NotBlank(message = "Settlement period is required")
    @JsonProperty("settlement_period")
    private String settlementPeriod;
}
