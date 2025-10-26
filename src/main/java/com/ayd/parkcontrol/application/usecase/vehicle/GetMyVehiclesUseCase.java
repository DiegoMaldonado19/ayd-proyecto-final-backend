package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetMyVehiclesUseCase {

    private final JpaVehicleRepository vehicleRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final VehicleDtoMapper mapper;

    @Transactional(readOnly = true)
    public List<VehicleResponse> execute(Long userId) {
        log.debug("Fetching vehicles for user ID: {}", userId);

        List<VehicleEntity> vehicles = vehicleRepository.findByUserIdAndIsActive(userId, true);

        Map<Integer, VehicleTypeEntity> vehicleTypes = vehicleTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(VehicleTypeEntity::getId, vt -> vt));

        return vehicles.stream()
                .map(vehicle -> mapper.toResponse(
                        vehicle,
                        vehicleTypes.get(vehicle.getVehicleTypeId())))
                .collect(Collectors.toList());
    }
}
