package com.ayd.parkcontrol.application.dto.response.fleet;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleTypeResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetVehicleResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("license_plate")
    private String licensePlate;

    @JsonProperty("plan")
    private SubscriptionPlanResponse plan;

    @JsonProperty("vehicle_type")
    private VehicleTypeResponse vehicleType;

    @JsonProperty("assigned_employee")
    private String assignedEmployee;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
