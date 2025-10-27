package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.RevenueTodayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GetRevenueTodayUseCase {

    @Transactional(readOnly = true)
    public RevenueTodayResponse execute() {
        return RevenueTodayResponse.builder()
                .totalRevenue(BigDecimal.ZERO)
                .ticketsRevenue(BigDecimal.ZERO)
                .subscriptionsRevenue(BigDecimal.ZERO)
                .totalTransactions(0L)
                .averageTicketValue(BigDecimal.ZERO)
                .build();
    }
}
