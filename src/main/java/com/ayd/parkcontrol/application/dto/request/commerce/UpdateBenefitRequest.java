package com.ayd.parkcontrol.application.dto.request.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBenefitRequest {

    @JsonProperty("benefit_type")
    private String benefitType;

    @JsonProperty("settlement_period")
    private String settlementPeriod;

    @JsonProperty("is_active")
    private Boolean isActive;
}
