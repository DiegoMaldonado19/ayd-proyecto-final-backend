package com.ayd.parkcontrol.application.dto.response.settlement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("business_id")
    private Long businessId;

    @JsonProperty("business_name")
    private String businessName;

    @JsonProperty("business_tax_id")
    private String businessTaxId;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("period_start")
    private LocalDateTime periodStart;

    @JsonProperty("period_end")
    private LocalDateTime periodEnd;

    @JsonProperty("total_hours")
    private BigDecimal totalHours;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("ticket_count")
    private Integer ticketCount;

    @JsonProperty("settled_at")
    private LocalDateTime settledAt;

    @JsonProperty("settled_by")
    private Long settledBy;

    @JsonProperty("settled_by_name")
    private String settledByName;

    @JsonProperty("observations")
    private String observations;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("tickets")
    private List<SettlementTicketDetail> tickets;
}
