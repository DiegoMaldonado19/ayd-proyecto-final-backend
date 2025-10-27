package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetDiscountsResponse;
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
public class GetFleetDiscountsUseCase {

    private final JpaFleetCompanyRepository fleetCompanyRepository;
    private final FleetDtoMapper fleetDtoMapper;

    @Transactional(readOnly = true)
    public FleetDiscountsResponse execute(Long id) {
        log.info("Getting discounts for fleet company id: {}", id);

        FleetCompanyEntity company = fleetCompanyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fleet company with id " + id + " not found"));

        return fleetDtoMapper.toDiscountsResponse(company);
    }
}
