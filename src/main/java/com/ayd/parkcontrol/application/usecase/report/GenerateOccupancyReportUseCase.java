package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.application.dto.response.report.OccupancyReportResponse;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateOccupancyReportUseCase {

    private final BranchRepository branchRepository;
    private final JpaTicketRepository ticketRepository;
    private final JpaTicketStatusTypeRepository ticketStatusTypeRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;

    @Transactional(readOnly = true)
    public List<OccupancyReportResponse> execute() {
        var inProgressStatus = ticketStatusTypeRepository.findByCode("IN_PROGRESS")
                .orElseThrow(() -> new RuntimeException("Estado IN_PROGRESS no encontrado"));
        var vehicleType2R = vehicleTypeRepository.findByCode("2R")
                .orElseThrow(() -> new RuntimeException("Tipo de vehículo 2R no encontrado"));
        var vehicleType4R = vehicleTypeRepository.findByCode("4R")
                .orElseThrow(() -> new RuntimeException("Tipo de vehículo 4R no encontrado"));

        var branches = branchRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        List<OccupancyReportResponse> report = new ArrayList<>();

        for (var branch : branches) {
            int capacity2R = branch.getCapacity2r() != null ? branch.getCapacity2r() : 0;
            int capacity4R = branch.getCapacity4r() != null ? branch.getCapacity4r() : 0;

            long occupied2R = ticketRepository.countActiveTicketsByBranchAndVehicleType(
                    branch.getId(), vehicleType2R.getId(), inProgressStatus.getId());
            long occupied4R = ticketRepository.countActiveTicketsByBranchAndVehicleType(
                    branch.getId(), vehicleType4R.getId(), inProgressStatus.getId());

            // Reporte para vehículos 2R
            if (capacity2R > 0) {
                double occupancy2R = (occupied2R * 100.0) / capacity2R;
                report.add(OccupancyReportResponse.builder()
                        .branchId(branch.getId())
                        .branchName(branch.getName())
                        .vehicleType("2R")
                        .totalCapacity(capacity2R)
                        .currentOccupancy((int) occupied2R)
                        .peakOccupancy((int) occupied2R) // Para simplificar, usamos el actual como pico
                        .averageOccupancy((double) occupied2R)
                        .occupancyPercentage(occupancy2R)
                        .build());
            }

            // Reporte para vehículos 4R
            if (capacity4R > 0) {
                double occupancy4R = (occupied4R * 100.0) / capacity4R;
                report.add(OccupancyReportResponse.builder()
                        .branchId(branch.getId())
                        .branchName(branch.getName())
                        .vehicleType("4R")
                        .totalCapacity(capacity4R)
                        .currentOccupancy((int) occupied4R)
                        .peakOccupancy((int) occupied4R)
                        .averageOccupancy((double) occupied4R)
                        .occupancyPercentage(occupancy4R)
                        .build());
            }
        }

        return report;
    }
}
