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
public class Subscription {
    private Long id;
    private Long userId;
    private Long planId;
    private String licensePlate;
    private BigDecimal frozenRateBase;
    private LocalDateTime purchaseDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal consumedHours;
    private Integer statusTypeId;
    private String statusName;
    private Boolean isAnnual;
    private Boolean notified80Percent;
    private Boolean autoRenewEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private SubscriptionPlan plan;
    private String userEmail;
    private String userName;
}
