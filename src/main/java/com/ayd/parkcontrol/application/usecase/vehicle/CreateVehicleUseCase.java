package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.request.vehicle.CreateVehicleRequest;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicateLicensePlateException;
import com.ayd.parkcontrol.domain.exception.VehicleTypeNotFoundException;
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
public class CreateVehicleUseCase {

    private final JpaVehicleRepository vehicleRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final VehicleDtoMapper mapper;

    @Transactional
    public VehicleResponse execute(CreateVehicleRequest request, Long userId) {
        log.debug("Creating vehicle for user ID: {} with plate: {}", userId, request.getLicensePlate());

        String normalizedPlate = request.getLicensePlate().toUpperCase();

        if (vehicleRepository.existsByUserIdAndLicensePlate(userId, normalizedPlate)) {
            throw new DuplicateLicensePlateException(
                    "Vehicle with license plate " + normalizedPlate + " already exists for this user");
        }

        VehicleTypeEntity vehicleType = vehicleTypeRepository.findById(request.getVehicleTypeId())
                .orElseThrow(() -> new VehicleTypeNotFoundException(
                        "Vehicle type not found with ID: " + request.getVehicleTypeId()));

        VehicleEntity vehicle = VehicleEntity.builder()
                .userId(userId)
                .licensePlate(normalizedPlate)
                .vehicleTypeId(request.getVehicleTypeId())
                .brand(request.getBrand())
                .model(request.getModel())
                .color(request.getColor())
                .year(request.getYear())
                .isActive(true)
                .build();

        VehicleEntity saved = vehicleRepository.save(vehicle);

        log.info("Vehicle created successfully with ID: {} for user: {}", saved.getId(), userId);

        return mapper.toResponse(saved, vehicleType);
    }
}
