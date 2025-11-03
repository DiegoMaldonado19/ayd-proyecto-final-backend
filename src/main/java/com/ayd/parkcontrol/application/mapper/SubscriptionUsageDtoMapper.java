package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionOverageResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionUsageResponse;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionOverage;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionUsage;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionUsageDtoMapper {

    public SubscriptionUsageResponse toResponse(SubscriptionUsage domain) {
        if (domain == null) {
            return null;
        }

        return SubscriptionUsageResponse.builder()
                .subscriptionId(domain.getSubscriptionId())
                .ticketId(domain.getTicketId())
                .folio(domain.getFolio())
                .branchName(domain.getBranchName())
                .entryTime(domain.getEntryTime())
                .exitTime(domain.getExitTime())
                .hoursConsumed(domain.getHoursConsumed())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public SubscriptionOverageResponse toOverageResponse(SubscriptionOverage domain) {
        if (domain == null) {
            return null;
        }

        return SubscriptionOverageResponse.builder()
                .overageId(domain.getId())
                .subscriptionId(domain.getSubscriptionId())
                .ticketId(domain.getTicketId())
                .folio(domain.getFolio())
                .branchName(domain.getBranchName())
                .overageHours(domain.getOverageHours())
                .rateApplied(domain.getAppliedRate())
                .amountCharged(domain.getChargedAmount())
                .chargeDate(domain.getChargedAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
