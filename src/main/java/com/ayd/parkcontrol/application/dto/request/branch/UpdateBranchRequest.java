package com.ayd.parkcontrol.application.dto.request.branch;

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
public class UpdateBranchRequest {

    @Size(min = 3, max = 200, message = "Branch name must be between 3 and 200 characters")
    @JsonProperty("name")
    private String name;

    @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters")
    @JsonProperty("address")
    private String address;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Opening time must be in format HH:mm")
    @JsonProperty("opening_time")
    private String openingTime;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Closing time must be in format HH:mm")
    @JsonProperty("closing_time")
    private String closingTime;

    @DecimalMin(value = "0.01", message = "Rate per hour must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Rate per hour must have at most 8 integer digits and 2 decimal places")
    @JsonProperty("rate_per_hour")
    private BigDecimal ratePerHour;

    @JsonProperty("is_active")
    private Boolean isActive;
}
