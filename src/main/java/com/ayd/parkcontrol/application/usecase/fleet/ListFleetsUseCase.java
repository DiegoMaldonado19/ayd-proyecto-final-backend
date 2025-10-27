package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListFleetsUseCase {

    private final JpaFleetCompanyRepository fleetCompanyRepository;
    private final FleetDtoMapper fleetDtoMapper;

    @Transactional(readOnly = true)
    public Page<FleetResponse> execute(Pageable pageable) {
        log.info("Listing fleet companies with pagination: {}", pageable);

        Page<FleetCompanyEntity> companies = fleetCompanyRepository.findAll(pageable);

        return companies.map(company -> {
            Long activeVehiclesCount = fleetCompanyRepository.countActiveVehiclesByCompanyId(company.getId());
            return fleetDtoMapper.toResponse(company, activeVehiclesCount);
        });
    }

    @Transactional(readOnly = true)
    public Page<FleetResponse> executeByActive(Boolean isActive, Pageable pageable) {
        log.info("Listing fleet companies with is_active: {} and pagination: {}", isActive, pageable);

        Page<FleetCompanyEntity> companies = fleetCompanyRepository.findAllByIsActive(isActive, pageable);

        return companies.map(company -> {
            Long activeVehiclesCount = fleetCompanyRepository.countActiveVehiclesByCompanyId(company.getId());
            return fleetDtoMapper.toResponse(company, activeVehiclesCount);
        });
    }
}
