package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.response.ticket.TicketResponse;
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
public class GetTicketsByBranchUseCase {

    private final JpaTicketRepository ticketRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Transactional(readOnly = true)
    public List<TicketResponse> execute(Long branchId) {
        log.info("Getting tickets for branch: {}", branchId);

        // 1. Buscar tickets por sucursal
        List<TicketEntity> tickets = ticketRepository.findByBranchIdOrderByEntryTimeDesc(branchId);

        // 2. Obtener tipos de vehículos y estados para mapear
        List<VehicleTypeEntity> vehicleTypes = vehicleTypeRepository.findAll();
        Map<Integer, String> vehicleTypeMap = vehicleTypes.stream()
                .collect(Collectors.toMap(VehicleTypeEntity::getId, VehicleTypeEntity::getName));

        List<TicketStatusTypeEntity> statuses = ticketStatusTypeRepository.findAll();
        Map<Integer, String> statusMap = statuses.stream()
                .collect(Collectors.toMap(TicketStatusTypeEntity::getId, TicketStatusTypeEntity::getName));

        // 3. Mapear a DTO
        return tickets.stream()
                .map(ticket -> mapToResponse(ticket, vehicleTypeMap, statusMap))
                .collect(Collectors.toList());
    }

    private TicketResponse mapToResponse(TicketEntity ticket, Map<Integer, String> vehicleTypeMap,
            Map<Integer, String> statusMap) {
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
                .statusName(statusMap.getOrDefault(ticket.getStatusTypeId(), "Unknown"))
                .qrCode(ticket.getQrCode())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
