package com.ayd.parkcontrol.application.dto.response.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporalPermitResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("subscription_id")
    private Long subscriptionId;

    @JsonProperty("temporal_plate")
    private String temporalPlate;

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("end_date")
    private LocalDateTime endDate;

    @JsonProperty("max_uses")
    private Integer maxUses;

    @JsonProperty("current_uses")
    private Integer currentUses;

    @JsonProperty("allowed_branches")
    private List<Long> allowedBranches;

    @JsonProperty("vehicle_type")
    private String vehicleType;

    @JsonProperty("status")
    private String status;

    @JsonProperty("approved_by")
    private String approvedBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
