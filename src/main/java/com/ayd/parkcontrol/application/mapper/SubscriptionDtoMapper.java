package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionBalanceResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class SubscriptionDtoMapper {

    public SubscriptionPlanResponse toSubscriptionPlanResponse(SubscriptionPlan domain) {
        if (domain == null) {
            return null;
        }

        return SubscriptionPlanResponse.builder()
                .id(domain.getId())
                .planTypeId(domain.getPlanTypeId())
                .planTypeName(domain.getPlanTypeName())
                .planTypeCode(domain.getPlanTypeCode())
                .monthlyHours(domain.getMonthlyHours())
                .monthlyDiscountPercentage(domain.getMonthlyDiscountPercentage())
                .annualAdditionalDiscountPercentage(domain.getAnnualAdditionalDiscountPercentage())
                .description(domain.getDescription())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public SubscriptionResponse toSubscriptionResponse(Subscription domain) {
        if (domain == null) {
            return null;
        }

        return SubscriptionResponse.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .userEmail(domain.getUserEmail())
                .userName(domain.getUserName())
                .plan(toSubscriptionPlanResponse(domain.getPlan()))
                .licensePlate(domain.getLicensePlate())
                .frozenRateBase(domain.getFrozenRateBase())
                .purchaseDate(domain.getPurchaseDate())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .consumedHours(domain.getConsumedHours())
                .statusName(domain.getStatusName())
                .isAnnual(domain.getIsAnnual())
                .autoRenewEnabled(domain.getAutoRenewEnabled())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public SubscriptionBalanceResponse toBalanceResponse(Subscription subscription) {
        if (subscription == null || subscription.getPlan() == null) {
            return null;
        }

        BigDecimal monthlyHours = BigDecimal.valueOf(subscription.getPlan().getMonthlyHours());
        BigDecimal consumedHours = subscription.getConsumedHours() != null ? subscription.getConsumedHours()
                : BigDecimal.ZERO;
        BigDecimal remainingHours = monthlyHours.subtract(consumedHours);

        BigDecimal percentage = BigDecimal.ZERO;
        if (monthlyHours.compareTo(BigDecimal.ZERO) > 0) {
            percentage = consumedHours
                    .divide(monthlyHours, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        long daysUntilExpiration = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());

        return SubscriptionBalanceResponse.builder()
                .subscriptionId(subscription.getId())
                .monthlyHours(subscription.getPlan().getMonthlyHours())
                .consumedHours(consumedHours)
                .remainingHours(remainingHours)
                .consumptionPercentage(percentage)
                .daysUntilExpiration(daysUntilExpiration)
                .build();
    }
}
