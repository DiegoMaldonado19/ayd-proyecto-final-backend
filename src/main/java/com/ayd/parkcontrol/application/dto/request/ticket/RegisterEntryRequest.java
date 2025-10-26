package com.ayd.parkcontrol.application.dto.request.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterEntryRequest {

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotBlank(message = "License plate is required")
    @Pattern(regexp = "^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$", message = "Invalid license plate format")
    private String licensePlate;

    @NotNull(message = "Vehicle type ID is required")
    private Integer vehicleTypeId;
}
