package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleTypeResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class VehicleDtoMapper {

    public VehicleResponse toResponse(VehicleEntity vehicle, VehicleTypeEntity vehicleType) {
        if (vehicle == null) {
            return null;
        }

        return VehicleResponse.builder()
                .id(vehicle.getId())
                .userId(vehicle.getUserId())
                .licensePlate(vehicle.getLicensePlate())
                .vehicleType(toVehicleTypeResponse(vehicleType))
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .color(vehicle.getColor())
                .year(vehicle.getYear())
                .isActive(vehicle.getIsActive())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }

    public VehicleTypeResponse toVehicleTypeResponse(VehicleTypeEntity vehicleType) {
        if (vehicleType == null) {
            return null;
        }

        return VehicleTypeResponse.builder()
                .id(vehicleType.getId())
                .code(vehicleType.getCode())
                .name(vehicleType.getName())
                .build();
    }
}
