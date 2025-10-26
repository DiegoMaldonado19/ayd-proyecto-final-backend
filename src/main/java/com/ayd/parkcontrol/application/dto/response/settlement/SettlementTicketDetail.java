package com.ayd.parkcontrol.application.dto.response.settlement;

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
public class SettlementTicketDetail {

    @JsonProperty("ticket_id")
    private Long ticketId;

    @JsonProperty("folio")
    private String folio;

    @JsonProperty("license_plate")
    private String licensePlate;

    @JsonProperty("free_hours_granted")
    private BigDecimal freeHoursGranted;
}
