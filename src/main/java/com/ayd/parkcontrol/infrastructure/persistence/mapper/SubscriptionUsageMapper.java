package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.subscription.SubscriptionOverage;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionUsage;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionOverageEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionUsageEntity;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionUsageMapper {

    public SubscriptionUsage toDomain(SubscriptionUsageEntity entity) {
        if (entity == null) {
            return null;
        }

        return SubscriptionUsage.builder()
                .id(entity.getId())
                .subscriptionId(entity.getSubscriptionId())
                .ticketId(entity.getId())
                .folio(entity.getFolio())
                .branchName(entity.getBranch() != null ? entity.getBranch().getName() : null)
                .entryTime(entity.getEntryTime())
                .exitTime(entity.getExitTime())
                .hoursConsumed(entity.getTotalHours())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public SubscriptionOverage toOverageDomain(SubscriptionOverageEntity entity) {
        if (entity == null) {
            return null;
        }

        String folio = null;

        if (entity.getTicket() != null) {
            folio = entity.getTicket().getFolio();
        }

        return SubscriptionOverage.builder()
                .id(entity.getId())
                .subscriptionId(entity.getSubscriptionId())
                .ticketId(entity.getTicketId())
                .folio(folio)
                .branchName(null)
                .overageHours(entity.getOverageHours())
                .chargedAmount(entity.getChargedAmount())
                .appliedRate(entity.getAppliedRate())
                .chargedAt(entity.getChargedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
