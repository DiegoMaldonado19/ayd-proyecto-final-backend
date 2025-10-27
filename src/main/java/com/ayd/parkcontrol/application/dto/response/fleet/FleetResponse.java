package com.ayd.parkcontrol.application.dto.response.fleet;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class FleetResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("tax_id")
    private String taxId;

    @JsonProperty("contact_name")
    private String contactName;

    @JsonProperty("corporate_email")
    private String corporateEmail;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("corporate_discount_percentage")
    private BigDecimal corporateDiscountPercentage;

    @JsonProperty("plate_limit")
    private Integer plateLimit;

    @JsonProperty("billing_period")
    private String billingPeriod;

    @JsonProperty("months_unpaid")
    private Integer monthsUnpaid;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("active_vehicles_count")
    private Long activeVehiclesCount;
}
