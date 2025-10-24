package com.ayd.parkcontrol.application.dto.response.branch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("address")
    private String address;

    @JsonProperty("opening_time")
    private String openingTime;

    @JsonProperty("closing_time")
    private String closingTime;

    @JsonProperty("capacity_2r")
    private Integer capacity2r;

    @JsonProperty("capacity_4r")
    private Integer capacity4r;

    @JsonProperty("rate_per_hour")
    private BigDecimal ratePerHour;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
