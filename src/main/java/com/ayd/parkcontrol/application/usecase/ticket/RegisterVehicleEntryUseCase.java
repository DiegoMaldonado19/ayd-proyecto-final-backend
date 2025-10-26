package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.request.ticket.RegisterEntryRequest;
import com.ayd.parkcontrol.application.dto.response.ticket.TicketResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.InsufficientCapacityException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterVehicleEntryUseCase {

    private final JpaTicketRepository ticketRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final JpaSubscriptionRepository subscriptionRepository;
    private final JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Transactional
    public TicketResponse execute(RegisterEntryRequest request) {
        log.info("Registering vehicle entry for plate: {} at branch: {}", request.getLicensePlate(),
                request.getBranchId());

        // 1. Validar que la sucursal existe y está activa
        BranchEntity branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new NotFoundException("Branch not found with ID: " + request.getBranchId()));

        if (!branch.getIsActive()) {
            throw new BusinessRuleException("Branch is not active");
        }

        // 2. Validar tipo de vehículo
        VehicleTypeEntity vehicleType = vehicleTypeRepository.findById(request.getVehicleTypeId())
                .orElseThrow(
                        () -> new NotFoundException("Vehicle type not found with ID: " + request.getVehicleTypeId()));

        // 3. Obtener estado "IN_PROGRESS"
        TicketStatusTypeEntity inProgressStatus = ticketStatusTypeRepository.findByCode("IN_PROGRESS")
                .orElseThrow(() -> new NotFoundException("Ticket status 'IN_PROGRESS' not found"));

        // 4. Validar capacidad disponible
        Long currentOccupancy = ticketRepository.countActiveTicketsByBranchAndVehicleType(
                request.getBranchId(),
                request.getVehicleTypeId(),
                inProgressStatus.getId());

        Integer capacity = vehicleType.getCode().equals("2R") ? branch.getCapacity2r() : branch.getCapacity4r();

        if (currentOccupancy >= capacity) {
            throw new InsufficientCapacityException(
                    String.format("Insufficient capacity for vehicle type %s at branch %s. Current: %d, Max: %d",
                            vehicleType.getName(), branch.getName(), currentOccupancy, capacity));
        }

        // 5. Verificar si el vehículo ya tiene un ticket activo en esta sucursal
        Boolean hasActiveTicket = ticketRepository.existsActiveTicketForPlateInBranch(
                request.getLicensePlate().toUpperCase(),
                request.getBranchId(),
                inProgressStatus.getId());

        if (hasActiveTicket) {
            throw new BusinessRuleException("Vehicle already has an active ticket at this branch");
        }

        // 6. Verificar si tiene suscripción activa
        SubscriptionEntity activeSubscription = subscriptionRepository
                .findActiveLicensePlateSubscription(request.getLicensePlate().toUpperCase())
                .orElse(null);

        // 7. Generar folio único
        String folio = generateUniqueFolio(request.getBranchId());

        // 8. Generar QR code
        String qrCode = generateQRCode(folio, request.getLicensePlate());

        // 9. Crear ticket
        TicketEntity ticket = TicketEntity.builder()
                .branchId(request.getBranchId())
                .folio(folio)
                .licensePlate(request.getLicensePlate().toUpperCase())
                .vehicleTypeId(request.getVehicleTypeId())
                .entryTime(LocalDateTime.now())
                .subscriptionId(activeSubscription != null ? activeSubscription.getId() : null)
                .isSubscriber(activeSubscription != null)
                .hasIncident(false)
                .statusTypeId(inProgressStatus.getId())
                .qrCode(qrCode)
                .build();

        TicketEntity savedTicket = ticketRepository.save(ticket);

        log.info("Vehicle entry registered successfully. Ticket ID: {}, Folio: {}", savedTicket.getId(),
                savedTicket.getFolio());

        return mapToResponse(savedTicket, vehicleType.getName(), inProgressStatus.getName());
    }

    private String generateUniqueFolio(Long branchId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return String.format("T-%d-%s", branchId, timestamp.substring(timestamp.length() - 8));
    }

    private String generateQRCode(String folio, String licensePlate) {
        return UUID.randomUUID().toString() + "-" + folio + "-" + licensePlate;
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
