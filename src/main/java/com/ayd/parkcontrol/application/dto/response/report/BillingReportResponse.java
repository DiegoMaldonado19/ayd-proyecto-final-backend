package com.ayd.parkcontrol.application.dto.response.report;

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
public class BillingReportResponse {

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("total_tickets")
    private Long totalTickets;

    @JsonProperty("total_revenue")
    private BigDecimal totalRevenue;

    @JsonProperty("average_ticket_value")
    private BigDecimal averageTicketValue;

    @JsonProperty("payment_method")
    private String paymentMethod;
}
