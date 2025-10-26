package com.ayd.parkcontrol.application.dto.request.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class UpdateTemporalPermitRequest {

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("end_date")
    private LocalDateTime endDate;

    @Min(value = 1, message = "max_uses must be at least 1")
    @Max(value = 20, message = "max_uses must not exceed 20")
    @JsonProperty("max_uses")
    private Integer maxUses;

    @JsonProperty("allowed_branches")
    private List<Long> allowedBranches;
}
