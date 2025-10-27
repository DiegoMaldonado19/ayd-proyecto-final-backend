package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateFleetsReportUseCase {

    private final JpaTicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> execute() {
        // Obtener todas las placas de flota (simplificado)
        var allTickets = ticketRepository.findAll();

        // Agrupar por placas más frecuentes (asumiendo que son flotas)
        Map<String, Long> plateFrequency = new HashMap<>();
        for (var ticket : allTickets) {
            if (ticket.getLicensePlate() != null) {
                plateFrequency.merge(ticket.getLicensePlate(), 1L, Long::sum);
            }
        }

        List<Map<String, Object>> fleetReport = new ArrayList<>();
        plateFrequency.forEach((plate, count) -> {
            if (count >= 3) { // Asumimos que 3+ entradas indica una flota
                BigDecimal totalHours = ticketRepository.sumHoursByLicensePlate(plate);
                BigDecimal totalAmount = ticketRepository.sumAmountByLicensePlate(plate);

                Map<String, Object> fleetData = new HashMap<>();
                fleetData.put("license_plate", plate);
                fleetData.put("total_entries", count);
                fleetData.put("total_hours", totalHours);
                fleetData.put("total_amount", totalAmount);

                fleetReport.add(fleetData);
            }
        });

        return fleetReport;
    }
}
