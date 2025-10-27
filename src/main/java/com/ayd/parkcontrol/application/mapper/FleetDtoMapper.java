package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetDiscountsResponse;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetResponse;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class FleetDtoMapper {

    private final VehicleDtoMapper vehicleDtoMapper;

    public FleetResponse toResponse(FleetCompanyEntity company, Long activeVehiclesCount) {
        if (company == null) {
            return null;
        }

        return FleetResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .taxId(company.getTaxId())
                .contactName(company.getContactName())
                .corporateEmail(company.getCorporateEmail())
                .phone(company.getPhone())
                .corporateDiscountPercentage(company.getCorporateDiscountPercentage())
                .plateLimit(company.getPlateLimit())
                .billingPeriod(company.getBillingPeriod())
                .monthsUnpaid(company.getMonthsUnpaid())
                .isActive(company.getIsActive())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .activeVehiclesCount(activeVehiclesCount != null ? activeVehiclesCount : 0L)
                .build();
    }

    public FleetVehicleResponse toVehicleResponse(FleetVehicleEntity vehicle) {
        if (vehicle == null) {
            return null;
        }

        return FleetVehicleResponse.builder()
                .id(vehicle.getId())
                .companyId(vehicle.getCompanyId())
                .licensePlate(vehicle.getLicensePlate())
                .plan(toPlanResponse(vehicle.getPlan()))
                .vehicleType(vehicleDtoMapper.toVehicleTypeResponse(vehicle.getVehicleType()))
                .assignedEmployee(vehicle.getAssignedEmployee())
                .isActive(vehicle.getIsActive())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }

    private SubscriptionPlanResponse toPlanResponse(
            com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionPlanEntity planEntity) {
        if (planEntity == null) {
            return null;
        }

        return SubscriptionPlanResponse.builder()
                .id(planEntity.getId())
                .planTypeId(planEntity.getPlanTypeId())
                .planTypeName(planEntity.getPlanType() != null ? planEntity.getPlanType().getName() : null)
                .planTypeCode(planEntity.getPlanType() != null ? planEntity.getPlanType().getCode() : null)
                .monthlyHours(planEntity.getMonthlyHours())
                .monthlyDiscountPercentage(planEntity.getMonthlyDiscountPercentage())
                .annualAdditionalDiscountPercentage(planEntity.getAnnualAdditionalDiscountPercentage())
                .description(planEntity.getDescription())
                .isActive(planEntity.getIsActive())
                .createdAt(planEntity.getCreatedAt())
                .updatedAt(planEntity.getUpdatedAt())
                .build();
    }

    public FleetDiscountsResponse toDiscountsResponse(FleetCompanyEntity company) {
        if (company == null) {
            return null;
        }

        return FleetDiscountsResponse.builder()
                .corporateDiscountPercentage(company.getCorporateDiscountPercentage())
                .maxTotalDiscount(new BigDecimal("35.00"))
                .build();
    }
}
