package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.response.ticket.TicketResponse;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetTicketByIdUseCase {

    private final JpaTicketRepository ticketRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Transactional(readOnly = true)
    public TicketResponse execute(Long ticketId) {
        log.info("Getting ticket by ID: {}", ticketId);

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found with ID: " + ticketId));

        VehicleTypeEntity vehicleType = vehicleTypeRepository.findById(ticket.getVehicleTypeId())
                .orElseThrow(() -> new NotFoundException("Vehicle type not found"));

        TicketStatusTypeEntity status = ticketStatusTypeRepository.findById(ticket.getStatusTypeId())
                .orElseThrow(() -> new NotFoundException("Ticket status not found"));

        return mapToResponse(ticket, vehicleType.getName(), status.getName());
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
