package com.ayd.parkcontrol.application.dto.response.branch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchScheduleResponse {

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("opening_time")
    private String openingTime;

    @JsonProperty("closing_time")
    private String closingTime;

    @JsonProperty("is_open_now")
    private Boolean isOpenNow;
}
