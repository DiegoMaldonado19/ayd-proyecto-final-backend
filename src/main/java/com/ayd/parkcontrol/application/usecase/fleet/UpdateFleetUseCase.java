package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.UpdateFleetRequest;
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
public class UpdateFleetUseCase {

    private final JpaFleetCompanyRepository fleetCompanyRepository;
    private final FleetDtoMapper fleetDtoMapper;

    @Transactional
    public FleetResponse execute(Long id, UpdateFleetRequest request) {
        log.info("Updating fleet company with id: {}", id);

        FleetCompanyEntity company = fleetCompanyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa flotillera no encontrada con ID: " + id));

        if (request.getName() != null) {
            company.setName(request.getName());
        }
        if (request.getContactName() != null) {
            company.setContactName(request.getContactName());
        }
        if (request.getCorporateEmail() != null) {
            company.setCorporateEmail(request.getCorporateEmail());
        }
        if (request.getPhone() != null) {
            company.setPhone(request.getPhone());
        }
        if (request.getCorporateDiscountPercentage() != null) {
            company.setCorporateDiscountPercentage(request.getCorporateDiscountPercentage());
        }
        if (request.getPlateLimit() != null) {
            Long currentActiveVehicles = fleetCompanyRepository.countActiveVehiclesByCompanyId(id);
            if (request.getPlateLimit() < currentActiveVehicles) {
                throw new IllegalArgumentException(
                        "Cannot set plate_limit below current active vehicles count: " + currentActiveVehicles);
            }
            company.setPlateLimit(request.getPlateLimit());
        }
        if (request.getBillingPeriod() != null) {
            company.setBillingPeriod(request.getBillingPeriod());
        }
        if (request.getIsActive() != null) {
            company.setIsActive(request.getIsActive());
        }

        FleetCompanyEntity updatedCompany = fleetCompanyRepository.save(company);
        log.info("Fleet company updated successfully with id: {}", updatedCompany.getId());

        Long activeVehiclesCount = fleetCompanyRepository.countActiveVehiclesByCompanyId(id);
        return fleetDtoMapper.toResponse(updatedCompany, activeVehiclesCount);
    }
}
