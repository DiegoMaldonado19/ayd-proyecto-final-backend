package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.domain.exception.VehicleNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetVehicleByIdUseCase {

    private final JpaVehicleRepository vehicleRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final VehicleDtoMapper mapper;

    @Transactional(readOnly = true)
    public VehicleResponse execute(Long vehicleId) {
        log.debug("Fetching vehicle with ID: {}", vehicleId);

        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(
                        "Vehicle not found with ID: " + vehicleId));

        VehicleTypeEntity vehicleType = vehicleTypeRepository.findById(vehicle.getVehicleTypeId())
                .orElse(null);

        return mapper.toResponse(vehicle, vehicleType);
    }
}
