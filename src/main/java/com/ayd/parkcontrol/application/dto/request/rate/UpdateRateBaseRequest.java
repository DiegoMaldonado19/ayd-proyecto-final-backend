package com.ayd.parkcontrol.application.dto.request.rate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRateBaseRequest {

    @NotNull(message = "amount_per_hour is required")
    @DecimalMin(value = "0.01", message = "amount_per_hour must be greater than 0")
    @JsonProperty("amount_per_hour")
    private BigDecimal amountPerHour;
}
