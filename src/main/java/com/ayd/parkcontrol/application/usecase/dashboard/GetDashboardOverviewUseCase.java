package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.DashboardOverviewResponse;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketChargeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaIncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetDashboardOverviewUseCase {

    private final BranchRepository branchRepository;
    private final PlateChangeRequestRepository plateChangeRequestRepository;
    private final JpaTicketRepository ticketRepository;
    private final JpaSubscriptionRepository subscriptionRepository;
    private final JpaTicketChargeRepository ticketChargeRepository;
    private final JpaTicketStatusTypeRepository ticketStatusTypeRepository;
    private final JpaIncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public DashboardOverviewResponse execute() {
        // Total de sucursales
        long totalBranches = branchRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();

        // Cambios de placa pendientes
        long pendingPlateChanges = plateChangeRequestRepository.findAll().size();

        // Obtener ID del estado "IN_PROGRESS" para tickets activos
        var inProgressStatus = ticketStatusTypeRepository.findByCode("IN_PROGRESS")
                .orElseThrow(() -> new RuntimeException("Estado IN_PROGRESS no encontrado"));

        // Total de tickets activos (en curso)
        long activeTickets = ticketRepository.findByStatusTypeId(inProgressStatus.getId()).size();

        // Suscripciones activas (no expiradas y con estado ACTIVE)
        long activeSubscriptions = subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getEndDate() != null && sub.getEndDate().isAfter(LocalDateTime.now()))
                .count();

        // Vehículos ingresados hoy
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long totalVehiclesToday = ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getEntryTime() != null
                        && !ticket.getEntryTime().isBefore(startOfDay)
                        && ticket.getEntryTime().isBefore(endOfDay))
                .count();

        // Ingresos del día actual
        var todayCharges = ticketChargeRepository.findAll().stream()
                .filter(charge -> charge.getCreatedAt() != null
                        && !charge.getCreatedAt().isBefore(startOfDay)
                        && charge.getCreatedAt().isBefore(endOfDay))
                .toList();

        BigDecimal revenueToday = todayCharges.stream()
                .map(charge -> charge.getTotalAmount() != null ? charge.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ocupación promedio
        var activeBranches = branchRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        double averageOccupancyPercentage = 0.0;
        if (!activeBranches.isEmpty()) {
            double totalOccupancy = 0.0;
            int branchesWithCapacity = 0;

            for (var branch : activeBranches) {
                int capacity2R = branch.getCapacity2r() != null ? branch.getCapacity2r() : 0;
                int capacity4R = branch.getCapacity4r() != null ? branch.getCapacity4r() : 0;
                int totalCapacity = capacity2R + capacity4R;

                if (totalCapacity > 0) {
                    // Contar tickets activos en esta sucursal
                    long currentOccupancy = ticketRepository.findByBranchIdAndStatusTypeId(
                            branch.getId(),
                            inProgressStatus.getId()).size();

                    double branchOccupancy = (currentOccupancy * 100.0) / totalCapacity;
                    totalOccupancy += branchOccupancy;
                    branchesWithCapacity++;
                }
            }

            if (branchesWithCapacity > 0) {
                averageOccupancyPercentage = totalOccupancy / branchesWithCapacity;
            }
        }

        // Incidentes pendientes (no resueltos)
        long pendingIncidents = incidentRepository.findAll().stream()
                .filter(incident -> incident.getIsResolved() != null && !incident.getIsResolved())
                .count();

        return DashboardOverviewResponse.builder()
                .totalBranches((int) totalBranches)
                .activeTickets(activeTickets)
                .activeSubscriptions(activeSubscriptions)
                .totalVehiclesToday(totalVehiclesToday)
                .revenueToday(revenueToday)
                .averageOccupancyPercentage(averageOccupancyPercentage)
                .pendingIncidents(pendingIncidents)
                .pendingPlateChanges(pendingPlateChanges)
                .build();
    }
}
