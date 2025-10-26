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
public class SubscriptionPlan {
    private Long id;
    private Integer planTypeId;
    private String planTypeName;
    private String planTypeCode;
    private Integer monthlyHours;
    private BigDecimal monthlyDiscountPercentage;
    private BigDecimal annualAdditionalDiscountPercentage;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
