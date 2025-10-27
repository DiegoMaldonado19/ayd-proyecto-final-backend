package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaIncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateIncidentsReportUseCase {

    private final JpaIncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> execute() {
        var incidents = incidentRepository.findAll();

        long pending = incidents.stream()
                .filter(incident -> incident.getIsResolved() != null && !incident.getIsResolved())
                .count();

        long resolved = incidents.stream()
                .filter(incident -> incident.getIsResolved() != null && incident.getIsResolved())
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("total_incidents", incidents.size());
        summary.put("pending_incidents", pending);
        summary.put("resolved_incidents", resolved);

        return List.of(summary);
    }
}
