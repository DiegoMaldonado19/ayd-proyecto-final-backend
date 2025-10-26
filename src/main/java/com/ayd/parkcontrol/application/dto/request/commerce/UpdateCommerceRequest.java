package com.ayd.parkcontrol.application.dto.request.commerce;

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
public class UpdateCommerceRequest {

    @Size(max = 200, message = "Name must not exceed 200 characters")
    @JsonProperty("name")
    private String name;

    @Size(max = 200, message = "Contact name must not exceed 200 characters")
    @JsonProperty("contact_name")
    private String contactName;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @JsonProperty("email")
    private String email;

    @Pattern(regexp = "^[0-9]{8,15}$", message = "Phone must be between 8 and 15 digits")
    @JsonProperty("phone")
    private String phone;

    @DecimalMin(value = "0.01", message = "Rate per hour must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Rate per hour must have at most 8 integer digits and 2 decimal places")
    @JsonProperty("rate_per_hour")
    private BigDecimal ratePerHour;

    @JsonProperty("is_active")
    private Boolean isActive;
}
