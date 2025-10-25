package com.ayd.parkcontrol.application.dto.response.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("plan_type_id")
    private Integer planTypeId;

    @JsonProperty("plan_type_name")
    private String planTypeName;

    @JsonProperty("plan_type_code")
    private String planTypeCode;

    @JsonProperty("monthly_hours")
    private Integer monthlyHours;

    @JsonProperty("monthly_discount_percentage")
    private BigDecimal monthlyDiscountPercentage;

    @JsonProperty("annual_additional_discount_percentage")
    private BigDecimal annualAdditionalDiscountPercentage;

    @JsonProperty("description")
    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
