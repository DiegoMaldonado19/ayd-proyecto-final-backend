package com.ayd.parkcontrol.application.dto.request.vehicle;

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
public class UpdateVehicleRequest {

    @Pattern(regexp = "^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$", message = "Invalid license plate format. Valid formats: ABC-123, ABC123, P-12345")
    @JsonProperty("license_plate")
    private String licensePlate;

    @Min(value = 1, message = "Vehicle type ID must be positive")
    @JsonProperty("vehicle_type_id")
    private Integer vehicleTypeId;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    @JsonProperty("brand")
    private String brand;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    @JsonProperty("model")
    private String model;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    @JsonProperty("color")
    private String color;

    @Min(value = 1900, message = "Year must be 1900 or later")
    @Max(value = 2026, message = "Year cannot be in the distant future")
    @JsonProperty("year")
    private Integer year;
}
