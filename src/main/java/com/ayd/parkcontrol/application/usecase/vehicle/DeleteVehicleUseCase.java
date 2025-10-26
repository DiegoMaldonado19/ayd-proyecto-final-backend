package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.domain.exception.VehicleNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteVehicleUseCase {

    private final JpaVehicleRepository vehicleRepository;

    @Transactional
    public void execute(Long vehicleId, Long userId) {
        log.debug("Deleting vehicle ID: {} for user ID: {}", vehicleId, userId);

        VehicleEntity vehicle = vehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new VehicleNotFoundException(
                        "Vehicle not found with ID: " + vehicleId + " for user: " + userId));

        vehicle.setIsActive(false);
        vehicleRepository.save(vehicle);

        log.info("Vehicle soft deleted successfully with ID: {} for user: {}", vehicleId, userId);
    }
}
