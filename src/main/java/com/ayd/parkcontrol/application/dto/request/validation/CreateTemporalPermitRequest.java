package com.ayd.parkcontrol.application.dto.request.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemporalPermitRequest {

    @NotNull(message = "subscription_id is required")
    @JsonProperty("subscription_id")
    private Long subscriptionId;

    @NotBlank(message = "temporal_plate is required")
    @Pattern(regexp = "^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$", message = "temporal_plate must be a valid license plate format")
    @JsonProperty("temporal_plate")
    private String temporalPlate;

    @NotNull(message = "start_date is required")
    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @NotNull(message = "end_date is required")
    @JsonProperty("end_date")
    private LocalDateTime endDate;

    @NotNull(message = "max_uses is required")
    @Min(value = 1, message = "max_uses must be at least 1")
    @Max(value = 20, message = "max_uses must not exceed 20")
    @JsonProperty("max_uses")
    private Integer maxUses;

    @JsonProperty("allowed_branches")
    private List<Long> allowedBranches;

    @NotNull(message = "vehicle_type_id is required")
    @JsonProperty("vehicle_type_id")
    private Integer vehicleTypeId;
}
