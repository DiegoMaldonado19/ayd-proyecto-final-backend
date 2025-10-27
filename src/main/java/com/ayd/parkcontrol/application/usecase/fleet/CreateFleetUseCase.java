package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.CreateFleetRequest;
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
public class CreateFleetUseCase {

    private final JpaFleetCompanyRepository fleetCompanyRepository;
    private final FleetDtoMapper fleetDtoMapper;

    @Transactional
    public FleetResponse execute(CreateFleetRequest request) {
        log.info("Creating fleet company with tax_id: {}", request.getTaxId());

        if (fleetCompanyRepository.existsByTaxId(request.getTaxId())) {
            throw new IllegalArgumentException("Fleet company with tax_id " + request.getTaxId() + " already exists");
        }

        FleetCompanyEntity company = FleetCompanyEntity.builder()
                .name(request.getName())
                .taxId(request.getTaxId())
                .contactName(request.getContactName())
                .corporateEmail(request.getCorporateEmail())
                .phone(request.getPhone())
                .corporateDiscountPercentage(request.getCorporateDiscountPercentage())
                .plateLimit(request.getPlateLimit())
                .billingPeriod(request.getBillingPeriod())
                .monthsUnpaid(0)
                .isActive(true)
                .build();

        FleetCompanyEntity savedCompany = fleetCompanyRepository.save(company);
        log.info("Fleet company created successfully with id: {}", savedCompany.getId());

        return fleetDtoMapper.toResponse(savedCompany, 0L);
    }
}
