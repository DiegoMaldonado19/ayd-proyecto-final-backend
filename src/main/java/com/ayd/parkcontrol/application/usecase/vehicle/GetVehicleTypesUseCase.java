package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleTypeResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetVehicleTypesUseCase {

    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final VehicleDtoMapper mapper;

    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> execute() {
        log.debug("Fetching all vehicle types");

        return vehicleTypeRepository.findAll()
                .stream()
                .map(mapper::toVehicleTypeResponse)
                .collect(Collectors.toList());
    }
}
