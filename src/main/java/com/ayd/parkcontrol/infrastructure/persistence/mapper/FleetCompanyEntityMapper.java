package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.fleet.FleetCompany;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import org.springframework.stereotype.Component;

@Component
public class FleetCompanyEntityMapper {

    public FleetCompany toDomain(FleetCompanyEntity entity) {
        if (entity == null) {
            return null;
        }

        return FleetCompany.builder()
                .id(entity.getId())
                .name(entity.getName())
                .taxId(entity.getTaxId())
                .contactName(entity.getContactName())
                .contactPhone(entity.getPhone())
                .corporateEmail(entity.getCorporateEmail())
                .billingAddress(null) // No existe en la entidad actual
                .maxVehicles(entity.getPlateLimit())
                .plateLimit(entity.getPlateLimit())
                .corporateDiscountPercentage(entity.getCorporateDiscountPercentage())
                .billingPeriod(entity.getBillingPeriod())
                .monthsUnpaid(entity.getMonthsUnpaid())
                .isActive(entity.getIsActive())
                .adminUserId(entity.getAdminUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public FleetCompanyEntity toEntity(FleetCompany domain) {
        if (domain == null) {
            return null;
        }

        return FleetCompanyEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .taxId(domain.getTaxId())
                .contactName(domain.getContactName())
                .corporateEmail(domain.getCorporateEmail())
                .phone(domain.getContactPhone())
                .plateLimit(domain.getPlateLimit() != null ? domain.getPlateLimit() : domain.getMaxVehicles())
                .corporateDiscountPercentage(domain.getCorporateDiscountPercentage())
                .billingPeriod(domain.getBillingPeriod())
                .monthsUnpaid(domain.getMonthsUnpaid())
                .isActive(domain.getIsActive())
                .adminUserId(domain.getAdminUserId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
