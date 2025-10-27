package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListFleetVehiclesUseCase {

    private final JpaFleetVehicleRepository fleetVehicleRepository;
    private final FleetDtoMapper fleetDtoMapper;

    @Transactional(readOnly = true)
    public Page<FleetVehicleResponse> execute(Long companyId, Pageable pageable) {
        log.info("Listing vehicles for fleet company id: {}", companyId);

        Page<FleetVehicleEntity> vehicles = fleetVehicleRepository.findByCompanyId(companyId, pageable);

        return vehicles.map(fleetDtoMapper::toVehicleResponse);
    }

    @Transactional(readOnly = true)
    public Page<FleetVehicleResponse> executeByActive(Long companyId, Boolean isActive, Pageable pageable) {
        log.info("Listing vehicles for fleet company id: {} with is_active: {}", companyId, isActive);

        Page<FleetVehicleEntity> vehicles = fleetVehicleRepository.findByCompanyIdAndIsActive(companyId, isActive,
                pageable);

        return vehicles.map(fleetDtoMapper::toVehicleResponse);
    }
}
