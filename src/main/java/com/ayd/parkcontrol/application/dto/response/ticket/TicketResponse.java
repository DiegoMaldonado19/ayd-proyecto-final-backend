package com.ayd.parkcontrol.application.dto.response.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private Long branchId;
    private String folio;
    private String licensePlate;
    private Integer vehicleTypeId;
    private String vehicleTypeName;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Long subscriptionId;
    private Boolean isSubscriber;
    private Boolean hasIncident;
    private Integer statusTypeId;
    private String statusName;
    private String qrCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
