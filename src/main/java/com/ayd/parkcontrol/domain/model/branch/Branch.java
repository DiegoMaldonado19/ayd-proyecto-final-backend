package com.ayd.parkcontrol.domain.model.branch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branch {
    private Long id;
    private String name;
    private String address;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Integer capacity2r;
    private Integer capacity4r;
    private BigDecimal ratePerHour;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
