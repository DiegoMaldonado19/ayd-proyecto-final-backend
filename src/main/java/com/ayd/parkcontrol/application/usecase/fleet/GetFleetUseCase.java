package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetFleetUseCase {

    private final JpaFleetCompanyRepository fleetCompanyRepository;
    private final FleetDtoMapper fleetDtoMapper;

    @Transactional(readOnly = true)
    public FleetResponse execute(Long id) {
        log.info("Getting fleet company with id: {}", id);

        FleetCompanyEntity company = fleetCompanyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa flotillera no encontrada con ID: " + id));

        Long activeVehiclesCount = fleetCompanyRepository.countActiveVehiclesByCompanyId(id);

        return fleetDtoMapper.toResponse(company, activeVehiclesCount);
    }
}
