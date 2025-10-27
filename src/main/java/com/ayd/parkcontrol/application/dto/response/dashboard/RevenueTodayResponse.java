package com.ayd.parkcontrol.application.dto.response.dashboard;

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
public class RevenueTodayResponse {

    @JsonProperty("total_revenue")
    private BigDecimal totalRevenue;

    @JsonProperty("tickets_revenue")
    private BigDecimal ticketsRevenue;

    @JsonProperty("subscriptions_revenue")
    private BigDecimal subscriptionsRevenue;

    @JsonProperty("total_transactions")
    private Long totalTransactions;

    @JsonProperty("average_ticket_value")
    private BigDecimal averageTicketValue;
}
