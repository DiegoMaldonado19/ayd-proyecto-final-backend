package com.ayd.parkcontrol.application.dto.response.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class SubscriptionOverageResponse {

    @JsonProperty("overage_id")
    private Long overageId;

    @JsonProperty("subscription_id")
    private Long subscriptionId;

    @JsonProperty("ticket_id")
    private Long ticketId;

    @JsonProperty("folio")
    private String folio;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("overage_hours")
    private BigDecimal overageHours;

    @JsonProperty("rate_applied")
    private BigDecimal rateApplied;

    @JsonProperty("amount_charged")
    private BigDecimal amountCharged;

    @JsonProperty("charge_date")
    private LocalDateTime chargeDate;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
