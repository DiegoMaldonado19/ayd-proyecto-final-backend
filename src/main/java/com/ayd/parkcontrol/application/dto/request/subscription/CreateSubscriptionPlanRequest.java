package com.ayd.parkcontrol.application.dto.request.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionPlanRequest {

    @NotNull(message = "plan_type_id is required")
    @JsonProperty("plan_type_id")
    private Integer planTypeId;

    @NotNull(message = "monthly_hours is required")
    @Min(value = 1, message = "monthly_hours must be at least 1")
    @JsonProperty("monthly_hours")
    private Integer monthlyHours;

    @NotNull(message = "monthly_discount_percentage is required")
    @DecimalMin(value = "0.00", message = "monthly_discount_percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "monthly_discount_percentage cannot exceed 100")
    @JsonProperty("monthly_discount_percentage")
    private BigDecimal monthlyDiscountPercentage;

    @NotNull(message = "annual_additional_discount_percentage is required")
    @DecimalMin(value = "0.00", message = "annual_additional_discount_percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "annual_additional_discount_percentage cannot exceed 100")
    @JsonProperty("annual_additional_discount_percentage")
    private BigDecimal annualAdditionalDiscountPercentage;

    @JsonProperty("description")
    private String description;
}
