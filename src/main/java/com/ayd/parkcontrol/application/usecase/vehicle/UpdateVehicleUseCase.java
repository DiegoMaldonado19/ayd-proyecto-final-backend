package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.request.vehicle.UpdateVehicleRequest;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicateLicensePlateException;
import com.ayd.parkcontrol.domain.exception.VehicleNotFoundException;
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
public class UpdateVehicleUseCase {

    private final JpaVehicleRepository vehicleRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final VehicleDtoMapper mapper;

    @Transactional
    public VehicleResponse execute(Long vehicleId, UpdateVehicleRequest request, Long userId) {
        log.debug("Updating vehicle ID: {} for user ID: {}", vehicleId, userId);

        VehicleEntity vehicle = vehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new VehicleNotFoundException(
                        "Vehicle not found with ID: " + vehicleId + " for user: " + userId));

        if (request.getLicensePlate() != null) {
            String normalizedPlate = request.getLicensePlate().toUpperCase();
            if (!normalizedPlate.equals(vehicle.getLicensePlate())) {
                if (vehicleRepository.existsByUserIdAndLicensePlateAndIdNot(userId, normalizedPlate, vehicleId)) {
                    throw new DuplicateLicensePlateException(
                            "Vehicle with license plate " + normalizedPlate + " already exists for this user");
                }
                vehicle.setLicensePlate(normalizedPlate);
            }
        }

        VehicleTypeEntity vehicleType = null;
        if (request.getVehicleTypeId() != null) {
            vehicleType = vehicleTypeRepository.findById(request.getVehicleTypeId())
                    .orElseThrow(() -> new VehicleTypeNotFoundException(
                            "Vehicle type not found with ID: " + request.getVehicleTypeId()));
            vehicle.setVehicleTypeId(request.getVehicleTypeId());
        } else {
            vehicleType = vehicleTypeRepository.findById(vehicle.getVehicleTypeId())
                    .orElseThrow(() -> new VehicleTypeNotFoundException(
                            "Vehicle type not found with ID: " + vehicle.getVehicleTypeId()));
        }

        if (request.getBrand() != null) {
            vehicle.setBrand(request.getBrand());
        }
        if (request.getModel() != null) {
            vehicle.setModel(request.getModel());
        }
        if (request.getColor() != null) {
            vehicle.setColor(request.getColor());
        }
        if (request.getYear() != null) {
            vehicle.setYear(request.getYear());
        }

        VehicleEntity updated = vehicleRepository.save(vehicle);

        log.info("Vehicle updated successfully with ID: {} for user: {}", updated.getId(), userId);

        return mapper.toResponse(updated, vehicleType);
    }
}
