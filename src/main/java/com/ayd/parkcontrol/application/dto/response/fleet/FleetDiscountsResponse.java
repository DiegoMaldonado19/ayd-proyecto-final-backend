package com.ayd.parkcontrol.application.dto.response.fleet;

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
public class FleetDiscountsResponse {

    @JsonProperty("corporate_discount_percentage")
    private BigDecimal corporateDiscountPercentage;

    @JsonProperty("max_total_discount")
    private BigDecimal maxTotalDiscount;
}
