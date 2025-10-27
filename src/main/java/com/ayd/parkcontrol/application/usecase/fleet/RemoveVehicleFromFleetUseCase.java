package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoveVehicleFromFleetUseCase {

    private final JpaFleetVehicleRepository fleetVehicleRepository;

    @Transactional
    public void execute(Long companyId, Long vehicleId) {
        log.info("Removing vehicle id: {} from fleet company id: {}", vehicleId, companyId);

        FleetVehicleEntity vehicle = fleetVehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet vehicle with id " + vehicleId + " not found"));

        if (!vehicle.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Vehicle does not belong to fleet company with id " + companyId);
        }

        vehicle.setIsActive(false);
        fleetVehicleRepository.save(vehicle);

        log.info("Vehicle removed successfully from fleet with id: {}", vehicleId);
    }
}
