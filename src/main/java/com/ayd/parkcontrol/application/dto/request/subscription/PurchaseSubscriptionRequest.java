package com.ayd.parkcontrol.application.dto.request.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseSubscriptionRequest {

    @NotNull(message = "plan_id is required")
    @JsonProperty("plan_id")
    private Long planId;

    @NotBlank(message = "license_plate is required")
    @Pattern(regexp = "^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$", message = "Invalid license plate format")
    @JsonProperty("license_plate")
    private String licensePlate;

    @NotNull(message = "is_annual is required")
    @JsonProperty("is_annual")
    private Boolean isAnnual;

    @JsonProperty("auto_renew_enabled")
    private Boolean autoRenewEnabled;
}
