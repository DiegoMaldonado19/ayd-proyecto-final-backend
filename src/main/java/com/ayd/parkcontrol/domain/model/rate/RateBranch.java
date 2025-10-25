package com.ayd.parkcontrol.domain.model.rate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateBranch {

    private Long branchId;
    private String branchName;
    private BigDecimal ratePerHour;
    private Boolean isActive;
}
