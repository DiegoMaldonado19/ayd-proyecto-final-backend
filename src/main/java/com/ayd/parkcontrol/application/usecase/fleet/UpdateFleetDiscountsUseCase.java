package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.UpdateFleetDiscountsRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetDiscountsResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateFleetDiscountsUseCase {

    private final JpaFleetCompanyRepository fleetCompanyRepository;
    private final FleetDtoMapper fleetDtoMapper;

    @Transactional
    public FleetDiscountsResponse execute(Long id, UpdateFleetDiscountsRequest request) {
        log.info("Updating discounts for fleet company id: {}", id);

        FleetCompanyEntity company = fleetCompanyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fleet company with id " + id + " not found"));

        // REGLA DE NEGOCIO: El descuento corporativo no puede ser mayor a 10%
        if (request.getCorporateDiscountPercentage().compareTo(new BigDecimal("10.00")) > 0) {
            throw new IllegalArgumentException(
                    "Corporate discount cannot exceed 10%. Requested: " + request.getCorporateDiscountPercentage());
        }

        // ADVERTENCIA: El descuento total (corporativo + plan + anual) no debe exceder
        // 35%
        // Esta validación completa se haría en el nivel de aplicación de descuentos al
        // calcular tickets
        // pero se advierte aquí si el descuento corporativo por sí solo es muy alto
        if (request.getCorporateDiscountPercentage().compareTo(new BigDecimal("35.00")) > 0) {
            throw new IllegalArgumentException(
                    "Corporate discount alone cannot exceed the maximum total discount of 35%");
        }

        company.setCorporateDiscountPercentage(request.getCorporateDiscountPercentage());
        FleetCompanyEntity updatedCompany = fleetCompanyRepository.save(company);

        log.info("Fleet discounts updated successfully for company id: {}", id);

        return fleetDtoMapper.toDiscountsResponse(updatedCompany);
    }
}
