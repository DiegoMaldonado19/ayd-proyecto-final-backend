package com.ayd.parkcontrol.application.dto.request.fleet;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddVehicleToFleetRequest {

    @NotBlank(message = "license_plate is required")
    @Size(max = 20, message = "license_plate cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$", message = "license_plate format is invalid")
    @JsonProperty("license_plate")
    private String licensePlate;

    @NotNull(message = "plan_id is required")
    @JsonProperty("plan_id")
    private Long planId;

    @NotNull(message = "vehicle_type_id is required")
    @JsonProperty("vehicle_type_id")
    private Integer vehicleTypeId;

    @Size(max = 200, message = "assigned_employee cannot exceed 200 characters")
    @JsonProperty("assigned_employee")
    private String assignedEmployee;
}
