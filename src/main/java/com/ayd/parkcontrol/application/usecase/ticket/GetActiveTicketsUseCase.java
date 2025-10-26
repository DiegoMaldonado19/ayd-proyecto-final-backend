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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetActiveTicketsUseCase {

    private final JpaTicketRepository ticketRepository;
    private final JpaTicketStatusTypeRepository ticketStatusTypeRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;

    @Transactional(readOnly = true)
    public List<TicketResponse> execute() {
        log.info("Getting all active tickets");

        // 1. Obtener estado "IN_PROGRESS"
        TicketStatusTypeEntity inProgressStatus = ticketStatusTypeRepository.findByCode("IN_PROGRESS")
                .orElseThrow(() -> new NotFoundException("Ticket status 'IN_PROGRESS' not found"));

        // 2. Buscar tickets activos
        List<TicketEntity> activeTickets = ticketRepository.findByStatusTypeId(inProgressStatus.getId());

        // 3. Obtener tipos de vehículos y estados para mapear
        List<VehicleTypeEntity> vehicleTypes = vehicleTypeRepository.findAll();
        Map<Integer, String> vehicleTypeMap = vehicleTypes.stream()
                .collect(Collectors.toMap(VehicleTypeEntity::getId, VehicleTypeEntity::getName));

        // 4. Mapear a DTO
        return activeTickets.stream()
                .map(ticket -> mapToResponse(ticket, vehicleTypeMap, inProgressStatus.getName()))
                .collect(Collectors.toList());
    }

    private TicketResponse mapToResponse(TicketEntity ticket, Map<Integer, String> vehicleTypeMap, String statusName) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .branchId(ticket.getBranchId())
                .folio(ticket.getFolio())
                .licensePlate(ticket.getLicensePlate())
                .vehicleTypeId(ticket.getVehicleTypeId())
                .vehicleTypeName(vehicleTypeMap.getOrDefault(ticket.getVehicleTypeId(), "Unknown"))
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
