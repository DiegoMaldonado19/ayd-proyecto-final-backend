package com.ayd.parkcontrol.domain.model.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUsage {
    private Long id;
    private Long subscriptionId;
    private Long ticketId;
    private String folio;
    private String branchName;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal hoursConsumed;
    private LocalDateTime createdAt;
}
