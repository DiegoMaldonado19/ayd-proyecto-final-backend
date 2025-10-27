package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.DashboardOverviewResponse;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketChargeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaIncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetDashboardByBranchUseCase {

    private final BranchRepository branchRepository;
    private final JpaTicketRepository ticketRepository;
    private final JpaTicketChargeRepository ticketChargeRepository;
    private final JpaTicketStatusTypeRepository ticketStatusTypeRepository;
    private final JpaIncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public DashboardOverviewResponse execute(Long branchId) {
        // Verificar que la sucursal existe
        var branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        // Obtener estado IN_PROGRESS
        var inProgressStatus = ticketStatusTypeRepository.findByCode("IN_PROGRESS")
                .orElseThrow(() -> new RuntimeException("Estado IN_PROGRESS no encontrado"));

        // Tickets activos en esta sucursal
        long activeTickets = ticketRepository.findByBranchIdAndStatusTypeId(
                branchId,
                inProgressStatus.getId()).size();

        // Vehículos ingresados hoy en esta sucursal
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        long totalVehiclesToday = ticketRepository.findByBranchIdOrderByEntryTimeDesc(branchId).stream()
                .filter(ticket -> ticket.getEntryTime() != null
                        && !ticket.getEntryTime().isBefore(startOfDay)
                        && ticket.getEntryTime().isBefore(endOfDay))
                .count();

        // Ingresos del día en esta sucursal
        var todayTickets = ticketRepository.findByBranchIdOrderByEntryTimeDesc(branchId).stream()
                .filter(ticket -> ticket.getEntryTime() != null
                        && !ticket.getEntryTime().isBefore(startOfDay)
                        && ticket.getEntryTime().isBefore(endOfDay))
                .toList();

        var todayTicketIds = todayTickets.stream().map(t -> t.getId()).toList();

        BigDecimal revenueToday = ticketChargeRepository.findAll().stream()
                .filter(charge -> todayTicketIds.contains(charge.getTicketId()))
                .map(charge -> charge.getTotalAmount() != null ? charge.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ocupación de esta sucursal
        int capacity2R = branch.getCapacity2r() != null ? branch.getCapacity2r() : 0;
        int capacity4R = branch.getCapacity4r() != null ? branch.getCapacity4r() : 0;
        int totalCapacity = capacity2R + capacity4R;

        double averageOccupancyPercentage = 0.0;
        if (totalCapacity > 0) {
            averageOccupancyPercentage = (activeTickets * 100.0) / totalCapacity;
        }

        // Incidentes pendientes en esta sucursal
        long pendingIncidents = incidentRepository.findAll().stream()
                .filter(incident -> incident.getBranchId() != null
                        && incident.getBranchId().equals(branchId)
                        && incident.getIsResolved() != null
                        && !incident.getIsResolved())
                .count();

        return DashboardOverviewResponse.builder()
                .totalBranches(1)
                .activeTickets(activeTickets)
                .activeSubscriptions(0L) // Las suscripciones son globales, no por sucursal
                .totalVehiclesToday(totalVehiclesToday)
                .revenueToday(revenueToday)
                .averageOccupancyPercentage(averageOccupancyPercentage)
                .pendingIncidents(pendingIncidents)
                .pendingPlateChanges(0L) // Los cambios de placa son globales
                .build();
    }
}
