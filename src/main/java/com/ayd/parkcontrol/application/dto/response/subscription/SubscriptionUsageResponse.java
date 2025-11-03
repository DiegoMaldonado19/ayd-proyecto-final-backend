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
public class SubscriptionUsageResponse {

    @JsonProperty("subscription_id")
    private Long subscriptionId;

    @JsonProperty("ticket_id")
    private Long ticketId;

    @JsonProperty("folio")
    private String folio;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("entry_time")
    private LocalDateTime entryTime;

    @JsonProperty("exit_time")
    private LocalDateTime exitTime;

    @JsonProperty("hours_consumed")
    private BigDecimal hoursConsumed;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
