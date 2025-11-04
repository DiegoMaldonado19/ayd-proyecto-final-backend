package com.ayd.parkcontrol.domain.model.fleet;

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
public class FleetCompany {
    private Long id;
    private String name;
    private String taxId;
    private String contactName;
    private String contactPhone;
    private String corporateEmail;
    private String billingAddress;
    private Integer maxVehicles;
    private Integer plateLimit;
    private BigDecimal corporateDiscountPercentage;
    private String billingPeriod;
    private Integer monthsUnpaid;
    private Boolean isActive;
    private Long adminUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
