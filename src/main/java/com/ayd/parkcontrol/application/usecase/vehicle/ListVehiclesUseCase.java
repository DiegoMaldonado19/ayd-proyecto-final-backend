package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListVehiclesUseCase {

    private final JpaVehicleRepository vehicleRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final VehicleDtoMapper mapper;

    @Transactional(readOnly = true)
    public Page<VehicleResponse> execute(Pageable pageable) {
        log.debug("Listing all active vehicles with pagination");

        Page<VehicleEntity> vehicles = vehicleRepository.findByIsActive(true, pageable);

        Map<Integer, VehicleTypeEntity> vehicleTypes = vehicleTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(VehicleTypeEntity::getId, vt -> vt));

        return vehicles.map(vehicle -> mapper.toResponse(
                vehicle,
                vehicleTypes.get(vehicle.getVehicleTypeId())));
    }
}
