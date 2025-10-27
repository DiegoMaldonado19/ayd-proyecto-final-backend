package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.RevenueTodayResponse;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketChargeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetRevenueTodayUseCase {

    private final JpaTicketChargeRepository ticketChargeRepository;
    private final JpaSubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public RevenueTodayResponse execute() {
        // Rango de tiempo: desde las 00:00 de hoy hasta ahora
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Ingresos por tickets (cobros del día)
        var todayTicketCharges = ticketChargeRepository.findAll().stream()
                .filter(charge -> charge.getCreatedAt() != null
                        && !charge.getCreatedAt().isBefore(startOfDay)
                        && charge.getCreatedAt().isBefore(endOfDay))
                .toList();

        BigDecimal ticketsRevenue = todayTicketCharges.stream()
                .map(charge -> charge.getTotalAmount() != null ? charge.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long ticketTransactions = todayTicketCharges.size();

        // Ingresos por suscripciones (compras del día)
        var todaySubscriptions = subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getPurchaseDate() != null
                        && !sub.getPurchaseDate().isBefore(startOfDay)
                        && sub.getPurchaseDate().isBefore(endOfDay))
                .toList();

        BigDecimal subscriptionsRevenue = todaySubscriptions.stream()
                .map(sub -> sub.getFrozenRateBase() != null ? sub.getFrozenRateBase() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long subscriptionTransactions = todaySubscriptions.size();

        // Totales
        BigDecimal totalRevenue = ticketsRevenue.add(subscriptionsRevenue);
        long totalTransactions = ticketTransactions + subscriptionTransactions;

        // Valor promedio del ticket
        BigDecimal averageTicketValue = BigDecimal.ZERO;
        if (ticketTransactions > 0) {
            averageTicketValue = ticketsRevenue.divide(
                    BigDecimal.valueOf(ticketTransactions),
                    2,
                    RoundingMode.HALF_UP);
        }

        return RevenueTodayResponse.builder()
                .totalRevenue(totalRevenue)
                .ticketsRevenue(ticketsRevenue)
                .subscriptionsRevenue(subscriptionsRevenue)
                .totalTransactions(totalTransactions)
                .averageTicketValue(averageTicketValue)
                .build();
    }
}
