package com.ayd.parkcontrol.domain.model.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeCharge {

    private BigDecimal amount;
    private String reason;
    private String reasonCode;

    public boolean hasCharge() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static AdministrativeCharge noCharge() {
        return AdministrativeCharge.builder()
                .amount(BigDecimal.ZERO)
                .reason("Sin cargo administrativo aplicable")
                .reasonCode("NO_CHARGE")
                .build();
    }
}
