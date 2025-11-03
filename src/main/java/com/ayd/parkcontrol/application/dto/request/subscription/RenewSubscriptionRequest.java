package com.ayd.parkcontrol.application.dto.request.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewSubscriptionRequest {

    @NotNull(message = "is_annual is required")
    @JsonProperty("is_annual")
    private Boolean isAnnual;

    @JsonProperty("auto_renew_enabled")
    private Boolean autoRenewEnabled;
}
