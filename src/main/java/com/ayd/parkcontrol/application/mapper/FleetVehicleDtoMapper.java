package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.domain.model.fleet.FleetVehicle;
import org.springframework.stereotype.Component;

@Component
public class FleetVehicleDtoMapper {

    public FleetVehicleResponse toResponse(FleetVehicle fleetVehicle) {
        if (fleetVehicle == null) {
            return null;
        }

        return FleetVehicleResponse.builder()
                .id(fleetVehicle.getId())
                .companyId(fleetVehicle.getCompanyId())
                .licensePlate(fleetVehicle.getLicensePlate())
                .assignedEmployee(fleetVehicle.getDriverName())
                .isActive(fleetVehicle.getIsActive())
                .createdAt(fleetVehicle.getCreatedAt())
                .updatedAt(fleetVehicle.getUpdatedAt())
                .build();
    }
}
