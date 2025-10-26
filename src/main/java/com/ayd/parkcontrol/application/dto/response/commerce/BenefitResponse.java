package com.ayd.parkcontrol.application.dto.response.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("business_id")
    private Long businessId;

    @JsonProperty("business_name")
    private String businessName;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("benefit_type_code")
    private String benefitTypeCode;

    @JsonProperty("benefit_type_name")
    private String benefitTypeName;

    @JsonProperty("settlement_period_code")
    private String settlementPeriodCode;

    @JsonProperty("settlement_period_name")
    private String settlementPeriodName;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
