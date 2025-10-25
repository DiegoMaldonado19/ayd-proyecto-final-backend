package com.ayd.parkcontrol.application.dto.response.subscription;

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
public class SubscriptionResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("plan")
    private SubscriptionPlanResponse plan;

    @JsonProperty("license_plate")
    private String licensePlate;

    @JsonProperty("frozen_rate_base")
    private BigDecimal frozenRateBase;

    @JsonProperty("purchase_date")
    private LocalDateTime purchaseDate;

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("end_date")
    private LocalDateTime endDate;

    @JsonProperty("consumed_hours")
    private BigDecimal consumedHours;

    @JsonProperty("status_name")
    private String statusName;

    @JsonProperty("is_annual")
    private Boolean isAnnual;

    @JsonProperty("auto_renew_enabled")
    private Boolean autoRenewEnabled;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
