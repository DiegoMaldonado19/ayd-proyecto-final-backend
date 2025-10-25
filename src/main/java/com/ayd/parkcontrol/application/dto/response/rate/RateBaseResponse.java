package com.ayd.parkcontrol.application.dto.response.rate;

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
public class RateBaseResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("amount_per_hour")
    private BigDecimal amountPerHour;

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("end_date")
    private LocalDateTime endDate;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_by")
    private Long createdBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
