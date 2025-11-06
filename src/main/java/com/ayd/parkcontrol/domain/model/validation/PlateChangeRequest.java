package com.ayd.parkcontrol.domain.model.validation;

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
public class PlateChangeRequest {

    private Long id;
    private Long subscriptionId;
    private Long userId;
    private String oldLicensePlate;
    private String newLicensePlate;
    private Integer reasonId;
    private String notes;
    private Integer statusId;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewNotes;
    private Boolean hasAdministrativeCharge;
    private BigDecimal administrativeChargeAmount;
    private String administrativeChargeReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
