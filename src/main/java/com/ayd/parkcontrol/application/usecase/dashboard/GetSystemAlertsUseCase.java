package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.SystemAlertResponse;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaIncidentRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSystemAlertsUseCase {

    private final BranchRepository branchRepository;
    private final JpaTicketRepository ticketRepository;
    private final JpaTicketStatusTypeRepository ticketStatusTypeRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final JpaIncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public List<SystemAlertResponse> execute() {
        List<SystemAlertResponse> alerts = new ArrayList<>();

        // Obtener estado IN_PROGRESS
        var inProgressStatus = ticketStatusTypeRepository.findByCode("IN_PROGRESS")
                .orElseThrow(() -> new RuntimeException("Estado IN_PROGRESS no encontrado"));

        // Obtener tipos de vehículo
        var vehicleType2R = vehicleTypeRepository.findByCode("2R")
                .orElseThrow(() -> new RuntimeException("Tipo de vehículo 2R no encontrado"));
        var vehicleType4R = vehicleTypeRepository.findByCode("4R")
                .orElseThrow(() -> new RuntimeException("Tipo de vehículo 4R no encontrado"));

        // Alertas de ocupación crítica (>90%)
        var branches = branchRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        for (var branch : branches) {
            int capacity2R = branch.getCapacity2r() != null ? branch.getCapacity2r() : 0;
            int capacity4R = branch.getCapacity4r() != null ? branch.getCapacity4r() : 0;
            int totalCapacity = capacity2R + capacity4R;

            if (totalCapacity > 0) {
                long occupied2R = ticketRepository.countActiveTicketsByBranchAndVehicleType(
                        branch.getId(), vehicleType2R.getId(), inProgressStatus.getId());
                long occupied4R = ticketRepository.countActiveTicketsByBranchAndVehicleType(
                        branch.getId(), vehicleType4R.getId(), inProgressStatus.getId());
                long totalOccupied = occupied2R + occupied4R;
                double occupancyPercentage = (totalOccupied * 100.0) / totalCapacity;

                if (occupancyPercentage >= 90) {
                    alerts.add(SystemAlertResponse.builder()
                            .alertType("CRITICAL_OCCUPANCY")
                            .severity("HIGH")
                            .message(String.format("Sucursal %s tiene ocupación crítica: %.1f%%",
                                    branch.getName(), occupancyPercentage))
                            .branchId(branch.getId())
                            .branchName(branch.getName())
                            .createdAt(LocalDateTime.now())
                            .build());
                } else if (occupancyPercentage >= 80) {
                    alerts.add(SystemAlertResponse.builder()
                            .alertType("HIGH_OCCUPANCY")
                            .severity("MEDIUM")
                            .message(String.format("Sucursal %s tiene ocupación alta: %.1f%%",
                                    branch.getName(), occupancyPercentage))
                            .branchId(branch.getId())
                            .branchName(branch.getName())
                            .createdAt(LocalDateTime.now())
                            .build());
                }
            }
        }

        // Alertas de incidentes pendientes
        long pendingIncidents = incidentRepository.findAll().stream()
                .filter(incident -> incident.getIsResolved() != null && !incident.getIsResolved())
                .count();

        if (pendingIncidents > 0) {
            alerts.add(SystemAlertResponse.builder()
                    .alertType("PENDING_INCIDENTS")
                    .severity(pendingIncidents > 5 ? "HIGH" : "MEDIUM")
                    .message(String.format("Hay %d incidente(s) pendiente(s) de resolución", pendingIncidents))
                    .branchId(null)
                    .branchName(null)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        return alerts;
    }
}
