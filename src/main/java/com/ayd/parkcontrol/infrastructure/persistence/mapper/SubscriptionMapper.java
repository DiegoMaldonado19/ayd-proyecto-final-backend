package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionPlanEntity;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public SubscriptionPlan toDomain(SubscriptionPlanEntity entity) {
        if (entity == null) {
            return null;
        }

        return SubscriptionPlan.builder()
                .id(entity.getId())
                .planTypeId(entity.getPlanTypeId())
                .planTypeName(entity.getPlanType() != null ? entity.getPlanType().getName() : null)
                .planTypeCode(entity.getPlanType() != null ? entity.getPlanType().getCode() : null)
                .monthlyHours(entity.getMonthlyHours())
                .monthlyDiscountPercentage(entity.getMonthlyDiscountPercentage())
                .annualAdditionalDiscountPercentage(entity.getAnnualAdditionalDiscountPercentage())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public SubscriptionPlanEntity toEntity(SubscriptionPlan domain) {
        if (domain == null) {
            return null;
        }

        return SubscriptionPlanEntity.builder()
                .id(domain.getId())
                .planTypeId(domain.getPlanTypeId())
                .monthlyHours(domain.getMonthlyHours())
                .monthlyDiscountPercentage(domain.getMonthlyDiscountPercentage())
                .annualAdditionalDiscountPercentage(domain.getAnnualAdditionalDiscountPercentage())
                .description(domain.getDescription())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Subscription toDomain(SubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }

        return Subscription.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .planId(entity.getPlanId())
                .licensePlate(entity.getLicensePlate())
                .frozenRateBase(entity.getFrozenRateBase())
                .purchaseDate(entity.getPurchaseDate())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .consumedHours(entity.getConsumedHours())
                .statusTypeId(entity.getStatusTypeId())
                .statusName(entity.getStatus() != null ? entity.getStatus().getName() : null)
                .isAnnual(entity.getIsAnnual())
                .notified80Percent(entity.getNotified80Percent())
                .autoRenewEnabled(entity.getAutoRenewEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .plan(toDomain(entity.getPlan()))
                .userEmail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .userName(entity.getUser() != null
                        ? entity.getUser().getFirstName() + " " + entity.getUser().getLastName()
                        : null)
                .build();
    }

    public SubscriptionEntity toEntity(Subscription domain) {
        if (domain == null) {
            return null;
        }

        return SubscriptionEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .planId(domain.getPlanId())
                .licensePlate(domain.getLicensePlate())
                .frozenRateBase(domain.getFrozenRateBase())
                .purchaseDate(domain.getPurchaseDate())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .consumedHours(domain.getConsumedHours())
                .statusTypeId(domain.getStatusTypeId())
                .isAnnual(domain.getIsAnnual())
                .notified80Percent(domain.getNotified80Percent())
                .autoRenewEnabled(domain.getAutoRenewEnabled())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
