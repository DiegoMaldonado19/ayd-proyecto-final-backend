package com.ayd.parkcontrol.application.dto.request.fleet;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFleetRequest {

    @NotBlank(message = "name is required")
    @Size(max = 200, message = "name cannot exceed 200 characters")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "tax_id is required")
    @Size(max = 50, message = "tax_id cannot exceed 50 characters")
    @JsonProperty("tax_id")
    private String taxId;

    @Size(max = 200, message = "contact_name cannot exceed 200 characters")
    @JsonProperty("contact_name")
    private String contactName;

    @NotBlank(message = "corporate_email is required")
    @Email(message = "corporate_email must be valid")
    @Size(max = 255, message = "corporate_email cannot exceed 255 characters")
    @JsonProperty("corporate_email")
    private String corporateEmail;

    @Size(max = 20, message = "phone cannot exceed 20 characters")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "phone must contain 8-15 digits")
    @JsonProperty("phone")
    private String phone;

    @NotNull(message = "corporate_discount_percentage is required")
    @DecimalMin(value = "0.00", message = "corporate_discount_percentage must be at least 0")
    @DecimalMax(value = "10.00", message = "corporate_discount_percentage cannot exceed 10")
    @JsonProperty("corporate_discount_percentage")
    private BigDecimal corporateDiscountPercentage;

    @NotNull(message = "plate_limit is required")
    @Min(value = 1, message = "plate_limit must be at least 1")
    @Max(value = 50, message = "plate_limit cannot exceed 50")
    @JsonProperty("plate_limit")
    private Integer plateLimit;

    @NotBlank(message = "billing_period is required")
    @Pattern(regexp = "^(DAILY|WEEKLY|MONTHLY|ANNUAL)$", message = "billing_period must be DAILY, WEEKLY, MONTHLY or ANNUAL")
    @JsonProperty("billing_period")
    private String billingPeriod;
}
