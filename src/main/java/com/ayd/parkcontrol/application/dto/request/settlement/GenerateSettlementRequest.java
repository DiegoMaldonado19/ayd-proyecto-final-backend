package com.ayd.parkcontrol.application.dto.request.settlement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSettlementRequest {

    @NotNull(message = "Business ID is required")
    @JsonProperty("business_id")
    private Long businessId;

    @NotNull(message = "Branch ID is required")
    @JsonProperty("branch_id")
    private Long branchId;

    @NotNull(message = "Period start is required")
    @JsonProperty("period_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodStart;

    @NotNull(message = "Period end is required")
    @JsonProperty("period_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

    @JsonProperty("observations")
    private String observations;
}
