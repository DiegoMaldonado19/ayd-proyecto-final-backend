package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.response.ticket.TicketChargeResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculateTicketChargeUseCase {

    private final JpaTicketRepository ticketRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaRateBaseHistoryRepository rateBaseHistoryRepository;
    private final JpaBusinessFreeHoursRepository businessFreeHoursRepository;
    private final JpaSubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public TicketChargeResponse execute(Long ticketId) {
        log.info("Calculating charge for ticket ID: {}", ticketId);

        // 1. Obtener ticket
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found with ID: " + ticketId));

        // 2. Calcular tiempo total
        LocalDateTime exitTime = ticket.getExitTime() != null ? ticket.getExitTime() : LocalDateTime.now();
        long minutes = Duration.between(ticket.getEntryTime(), exitTime).toMinutes();
        BigDecimal totalHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        // 3. Obtener horas gratis otorgadas por comercios
        BigDecimal freeHoursGranted = businessFreeHoursRepository.sumGrantedHoursByTicketId(ticketId);

        // 4. Obtener tarifa aplicable
        BigDecimal rateApplied = getApplicableRate(ticket);

        // 5. Calcular según tipo de cliente
        if (ticket.getIsSubscriber() && ticket.getSubscriptionId() != null) {
            return calculateSubscriberCharge(ticket, totalHours, freeHoursGranted, rateApplied);
        } else {
            return calculateNonSubscriberCharge(ticketId, totalHours, freeHoursGranted, rateApplied);
        }
    }

    private BigDecimal getApplicableRate(TicketEntity ticket) {
        // 1. Verificar si es suscriptor con tarifa congelada
        if (ticket.getIsSubscriber() && ticket.getSubscriptionId() != null) {
            SubscriptionEntity subscription = subscriptionRepository.findById(ticket.getSubscriptionId())
                    .orElse(null);
            if (subscription != null && subscription.getFrozenRateBase() != null) {
                log.info("Using frozen rate from subscription: {}", subscription.getFrozenRateBase());
                return subscription.getFrozenRateBase();
            }
        }

        // 2. Verificar tarifa de sucursal
        BranchEntity branch = branchRepository.findById(ticket.getBranchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        if (branch.getRatePerHour() != null) {
            log.info("Using branch rate: {}", branch.getRatePerHour());
            return branch.getRatePerHour();
        }

        // 3. Usar tarifa base actual
        RateBaseHistoryEntity currentRate = rateBaseHistoryRepository.findCurrentRate()
                .orElseThrow(() -> new BusinessRuleException("No base rate configured"));

        log.info("Using base rate: {}", currentRate.getAmountPerHour());
        return currentRate.getAmountPerHour();
    }

    private TicketChargeResponse calculateNonSubscriberCharge(
            Long ticketId,
            BigDecimal totalHours,
            BigDecimal freeHoursGranted,
            BigDecimal rateApplied) {
        // Para no suscriptores, las horas gratis reducen directamente las horas
        // facturables
        BigDecimal billableHours = totalHours.subtract(freeHoursGranted).max(BigDecimal.ZERO);
        BigDecimal totalAmount = billableHours.multiply(rateApplied).setScale(2, RoundingMode.HALF_UP);

        return TicketChargeResponse.builder()
                .ticketId(ticketId)
                .totalHours(totalHours)
                .freeHoursGranted(freeHoursGranted)
                .billableHours(billableHours)
                .rateApplied(rateApplied)
                .subtotal(totalAmount)
                .subscriptionHoursConsumed(BigDecimal.ZERO)
                .subscriptionOverageHours(BigDecimal.ZERO)
                .subscriptionOverageCharge(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .build();
    }

    private TicketChargeResponse calculateSubscriberCharge(
            TicketEntity ticket,
            BigDecimal totalHours,
            BigDecimal freeHoursGranted,
            BigDecimal rateApplied) {
        SubscriptionEntity subscription = subscriptionRepository.findById(ticket.getSubscriptionId())
                .orElseThrow(() -> new NotFoundException("Subscription not found"));

        // Para suscriptores, las horas gratis pueden:
        // - No consumir del bolsón (si el comercio configuró "NO_CONSUME_HOURS")
        // En este caso, las horas gratis simplemente no se descuentan del bolsón

        // Horas disponibles en suscripción
        BigDecimal monthlyHours = BigDecimal.valueOf(subscription.getPlan().getMonthlyHours());
        BigDecimal hoursConsumed = subscription.getConsumedHours();
        BigDecimal availableHours = monthlyHours.subtract(hoursConsumed).max(BigDecimal.ZERO);

        // Si hay horas gratis, NO se consumen del bolsón
        BigDecimal hoursToConsume = totalHours.subtract(freeHoursGranted).max(BigDecimal.ZERO);

        BigDecimal subscriptionHoursConsumed;
        BigDecimal overageHours;

        if (hoursToConsume.compareTo(availableHours) <= 0) {
            // Todo está cubierto por la suscripción
            subscriptionHoursConsumed = hoursToConsume;
            overageHours = BigDecimal.ZERO;
        } else {
            // Hay excedente
            subscriptionHoursConsumed = availableHours;
            overageHours = hoursToConsume.subtract(availableHours);
        }

        BigDecimal overageCharge = overageHours.multiply(rateApplied).setScale(2, RoundingMode.HALF_UP);

        return TicketChargeResponse.builder()
                .ticketId(ticket.getId())
                .totalHours(totalHours)
                .freeHoursGranted(freeHoursGranted)
                .billableHours(hoursToConsume)
                .rateApplied(rateApplied)
                .subtotal(overageCharge)
                .subscriptionHoursConsumed(subscriptionHoursConsumed)
                .subscriptionOverageHours(overageHours)
                .subscriptionOverageCharge(overageCharge)
                .totalAmount(overageCharge)
                .build();
    }
}
