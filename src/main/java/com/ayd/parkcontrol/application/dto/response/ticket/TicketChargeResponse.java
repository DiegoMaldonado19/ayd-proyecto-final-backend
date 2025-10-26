package com.ayd.parkcontrol.application.dto.response.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketChargeResponse {

    private Long ticketId;
    private BigDecimal totalHours;
    private BigDecimal freeHoursGranted;
    private BigDecimal billableHours;
    private BigDecimal rateApplied;
    private BigDecimal subtotal;
    private BigDecimal subscriptionHoursConsumed;
    private BigDecimal subscriptionOverageHours;
    private BigDecimal subscriptionOverageCharge;
    private BigDecimal totalAmount;
}
