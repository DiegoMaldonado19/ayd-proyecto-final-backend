package com.ayd.parkcontrol.application.dto.response.ticket;

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
public class BusinessFreeHoursResponse {

    private Long id;
    private Long ticketId;
    private Long businessId;
    private String businessName;
    private Long branchId;
    private BigDecimal grantedHours;
    private LocalDateTime grantedAt;
    private Boolean isSettled;
}
