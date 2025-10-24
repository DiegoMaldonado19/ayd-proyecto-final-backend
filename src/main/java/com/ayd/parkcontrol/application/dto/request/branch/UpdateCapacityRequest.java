package com.ayd.parkcontrol.application.dto.request.branch;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCapacityRequest {

    @NotNull(message = "Capacity for 2-wheel vehicles is required")
    @Min(value = 1, message = "Capacity for 2-wheel vehicles must be at least 1")
    @Max(value = 1000, message = "Capacity for 2-wheel vehicles cannot exceed 1000")
    @JsonProperty("capacity_2r")
    private Integer capacity2r;

    @NotNull(message = "Capacity for 4-wheel vehicles is required")
    @Min(value = 1, message = "Capacity for 4-wheel vehicles must be at least 1")
    @Max(value = 1000, message = "Capacity for 4-wheel vehicles cannot exceed 1000")
    @JsonProperty("capacity_4r")
    private Integer capacity4r;
}
