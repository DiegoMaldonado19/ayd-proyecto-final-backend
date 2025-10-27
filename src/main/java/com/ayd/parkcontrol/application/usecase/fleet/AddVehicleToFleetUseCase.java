package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.AddVehicleToFleetRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetVehicleRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionPlanRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddVehicleToFleetUseCase {

    private final JpaFleetCompanyRepository fleetCompanyRepository;
    private final JpaFleetVehicleRepository fleetVehicleRepository;
    private final JpaSubscriptionPlanRepository subscriptionPlanRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final FleetDtoMapper fleetDtoMapper;

    @Transactional
    public FleetVehicleResponse execute(Long companyId, AddVehicleToFleetRequest request) {
        log.info("Adding vehicle to fleet company id: {}, license_plate: {}", companyId, request.getLicensePlate());

        if (!fleetCompanyRepository.existsById(companyId)) {
            throw new IllegalArgumentException("Fleet company with id " + companyId + " not found");
        }

        if (!subscriptionPlanRepository.existsById(request.getPlanId())) {
            throw new IllegalArgumentException("Subscription plan with id " + request.getPlanId() + " not found");
        }

        if (!vehicleTypeRepository.existsById(request.getVehicleTypeId())) {
            throw new IllegalArgumentException("Vehicle type with id " + request.getVehicleTypeId() + " not found");
        }

        if (fleetVehicleRepository.existsActiveLicensePlate(request.getLicensePlate())) {
            throw new IllegalArgumentException(
                    "License plate " + request.getLicensePlate() + " is already registered in another active fleet");
        }

        if (fleetVehicleRepository.existsByCompanyIdAndLicensePlate(companyId, request.getLicensePlate())) {
            throw new IllegalArgumentException(
                    "License plate " + request.getLicensePlate() + " is already registered in this fleet");
        }

        FleetVehicleEntity vehicle = FleetVehicleEntity.builder()
                .companyId(companyId)
                .licensePlate(request.getLicensePlate())
                .planId(request.getPlanId())
                .vehicleTypeId(request.getVehicleTypeId())
                .assignedEmployee(request.getAssignedEmployee())
                .isActive(true)
                .build();

        FleetVehicleEntity savedVehicle = fleetVehicleRepository.save(vehicle);
        log.info("Vehicle added successfully to fleet with id: {}", savedVehicle.getId());

        FleetVehicleEntity vehicleWithDetails = fleetVehicleRepository.findByIdWithDetails(savedVehicle.getId())
                .orElseThrow(() -> new IllegalStateException("Failed to load vehicle details"));

        return fleetDtoMapper.toVehicleResponse(vehicleWithDetails);
    }
}
