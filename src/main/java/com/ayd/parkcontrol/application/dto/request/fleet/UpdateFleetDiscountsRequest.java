package com.ayd.parkcontrol.application.dto.request.fleet;

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
public class UpdateFleetDiscountsRequest {

    @NotNull(message = "corporate_discount_percentage is required")
    @DecimalMin(value = "0.00", message = "corporate_discount_percentage must be at least 0")
    @DecimalMax(value = "10.00", message = "corporate_discount_percentage cannot exceed 10")
    @JsonProperty("corporate_discount_percentage")
    private BigDecimal corporateDiscountPercentage;
}
