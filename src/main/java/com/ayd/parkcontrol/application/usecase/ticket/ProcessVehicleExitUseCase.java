package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.response.ticket.TicketChargeResponse;
import com.ayd.parkcontrol.application.dto.response.ticket.TicketResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessVehicleExitUseCase {

    private final JpaTicketRepository ticketRepository;
    private final JpaTicketStatusTypeRepository ticketStatusTypeRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final JpaTicketChargeRepository ticketChargeRepository;
    private final JpaSubscriptionRepository subscriptionRepository;
    private final CalculateTicketChargeUseCase calculateTicketChargeUseCase;

    @Transactional
    public TicketResponse execute(Long ticketId) {
        log.info("Processing vehicle exit for ticket ID: {}", ticketId);

        // 1. Obtener ticket
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found with ID: " + ticketId));

        // 2. Validar que el ticket está en progreso
        TicketStatusTypeEntity inProgressStatus = ticketStatusTypeRepository.findByCode("IN_PROGRESS")
                .orElseThrow(() -> new NotFoundException("Ticket status 'IN_PROGRESS' not found"));

        if (!ticket.getStatusTypeId().equals(inProgressStatus.getId())) {
            throw new BusinessRuleException("Ticket is not in progress. Current status: " + ticket.getStatusTypeId());
        }

        // 3. Validar que no tenga salida registrada
        if (ticket.getExitTime() != null) {
            throw new BusinessRuleException("Ticket already has an exit time registered");
        }

        // 4. Registrar hora de salida
        ticket.setExitTime(LocalDateTime.now());

        // 5. Calcular cargo
        TicketChargeResponse charge = calculateTicketChargeUseCase.execute(ticketId);

        // 6. Guardar cargo en base de datos
        TicketChargeEntity chargeEntity = TicketChargeEntity.builder()
                .ticketId(ticketId)
                .totalHours(charge.getTotalHours())
                .freeHoursGranted(charge.getFreeHoursGranted())
                .billableHours(charge.getBillableHours())
                .rateApplied(charge.getRateApplied())
                .subtotal(charge.getSubtotal())
                .subscriptionHoursConsumed(charge.getSubscriptionHoursConsumed())
                .subscriptionOverageHours(charge.getSubscriptionOverageHours())
                .subscriptionOverageCharge(charge.getSubscriptionOverageCharge())
                .totalAmount(charge.getTotalAmount())
                .build();

        ticketChargeRepository.save(chargeEntity);

        // 7. Si es suscriptor, actualizar horas consumidas
        if (ticket.getIsSubscriber() && ticket.getSubscriptionId() != null) {
            SubscriptionEntity subscription = subscriptionRepository.findById(ticket.getSubscriptionId())
                    .orElseThrow(() -> new NotFoundException("Subscription not found"));

            subscription.setConsumedHours(
                    subscription.getConsumedHours().add(charge.getSubscriptionHoursConsumed()));
            subscriptionRepository.save(subscription);
        }

        // 8. Cambiar estado del ticket a COMPLETED
        TicketStatusTypeEntity completedStatus = ticketStatusTypeRepository.findByCode("COMPLETED")
                .orElseThrow(() -> new NotFoundException("Ticket status 'COMPLETED' not found"));

        ticket.setStatusTypeId(completedStatus.getId());
        TicketEntity savedTicket = ticketRepository.save(ticket);

        log.info("Vehicle exit processed successfully. Ticket ID: {}, Total charge: {}",
                ticketId, charge.getTotalAmount());

        // 9. Obtener información adicional para la respuesta
        VehicleTypeEntity vehicleType = vehicleTypeRepository.findById(ticket.getVehicleTypeId())
                .orElseThrow(() -> new NotFoundException("Vehicle type not found"));

        return mapToResponse(savedTicket, vehicleType.getName(), completedStatus.getName());
    }

    private TicketResponse mapToResponse(TicketEntity ticket, String vehicleTypeName, String statusName) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .branchId(ticket.getBranchId())
                .folio(ticket.getFolio())
                .licensePlate(ticket.getLicensePlate())
                .vehicleTypeId(ticket.getVehicleTypeId())
                .vehicleTypeName(vehicleTypeName)
                .entryTime(ticket.getEntryTime())
                .exitTime(ticket.getExitTime())
                .subscriptionId(ticket.getSubscriptionId())
                .isSubscriber(ticket.getIsSubscriber())
                .hasIncident(ticket.getHasIncident())
                .statusTypeId(ticket.getStatusTypeId())
                .statusName(statusName)
                .qrCode(ticket.getQrCode())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
