package com.ayd.parkcontrol.web.dto.response;

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
    private Long id;
    private Long companyId;
    private String licensePlate;
    private String brand;
    private String model;
    private Integer year;
    private String color;
    private String vehicleType;
    private String driverName;
    private String driverPhone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
