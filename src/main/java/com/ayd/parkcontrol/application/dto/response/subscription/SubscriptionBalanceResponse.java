package com.ayd.parkcontrol.application.dto.response.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionBalanceResponse {

    @JsonProperty("subscription_id")
    private Long subscriptionId;

    @JsonProperty("monthly_hours")
    private Integer monthlyHours;

    @JsonProperty("consumed_hours")
    private BigDecimal consumedHours;

    @JsonProperty("remaining_hours")
    private BigDecimal remainingHours;

    @JsonProperty("consumption_percentage")
    private BigDecimal consumptionPercentage;

    @JsonProperty("days_until_expiration")
    private Long daysUntilExpiration;
}
