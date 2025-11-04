package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.fleet.FleetVehicle;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import org.springframework.stereotype.Component;

@Component
public class FleetVehicleEntityMapper {

    public FleetVehicle toDomain(FleetVehicleEntity entity) {
        if (entity == null) {
            return null;
        }

        return FleetVehicle.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .licensePlate(entity.getLicensePlate())
                .brand(null) // No existe en la entidad actual
                .model(null) // No existe en la entidad actual
                .year(null) // No existe en la entidad actual
                .color(null) // No existe en la entidad actual
                .vehicleType(entity.getVehicleType() != null ? entity.getVehicleType().getName() : null)
                .driverName(entity.getAssignedEmployee())
                .driverPhone(null) // No existe en la entidad actual
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public FleetVehicleEntity toEntity(FleetVehicle domain) {
        if (domain == null) {
            return null;
        }

        return FleetVehicleEntity.builder()
                .id(domain.getId())
                .companyId(domain.getCompanyId())
                .licensePlate(domain.getLicensePlate())
                .assignedEmployee(domain.getDriverName())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
