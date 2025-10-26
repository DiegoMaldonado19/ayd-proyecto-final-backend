package com.ayd.parkcontrol.application.dto.request.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyBenefitRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotNull(message = "Granted hours is required")
    private Double grantedHours;
}
