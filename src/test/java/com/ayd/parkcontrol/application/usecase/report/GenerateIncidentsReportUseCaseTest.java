package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.infrastructure.persistence.entity.IncidentEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateIncidentsReportUseCaseTest {

    @Mock
    private JpaIncidentRepository incidentRepository;

    @InjectMocks
    private GenerateIncidentsReportUseCase generateIncidentsReportUseCase;

    private IncidentEntity pendingIncident;
    private IncidentEntity resolvedIncident;

    @BeforeEach
    void setUp() {
        pendingIncident = IncidentEntity.builder()
                .id(1L)
                .ticketId(1L)
                .incidentTypeId(1)
                .isResolved(false)
                .build();

        resolvedIncident = IncidentEntity.builder()
                .id(2L)
                .ticketId(2L)
                .incidentTypeId(1)
                .isResolved(true)
                .build();
    }

    @Test
    void execute_shouldGenerateIncidentsReport_whenIncidentsExist() {
        when(incidentRepository.findAll()).thenReturn(List.of(pendingIncident, resolvedIncident));

        List<Map<String, Object>> result = generateIncidentsReportUseCase.execute();

        assertThat(result).hasSize(1);
        Map<String, Object> summary = result.get(0);
        assertThat(summary.get("total_incidents")).isEqualTo(2);
        assertThat(summary.get("pending_incidents")).isEqualTo(1L);
        assertThat(summary.get("resolved_incidents")).isEqualTo(1L);
    }

    @Test
    void execute_shouldReturnZeroIncidents_whenNoIncidentsExist() {
        when(incidentRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = generateIncidentsReportUseCase.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("total_incidents")).isEqualTo(0);
        assertThat(result.get(0).get("pending_incidents")).isEqualTo(0L);
        assertThat(result.get(0).get("resolved_incidents")).isEqualTo(0L);
    }

    @Test
    void execute_shouldHandleNullIsResolved_whenCountingIncidents() {
        IncidentEntity nullResolved = IncidentEntity.builder()
                .id(3L)
                .ticketId(3L)
                .incidentTypeId(1)
                .isResolved(null)
                .build();

        when(incidentRepository.findAll()).thenReturn(List.of(pendingIncident, resolvedIncident, nullResolved));

        List<Map<String, Object>> result = generateIncidentsReportUseCase.execute();

        assertThat(result).hasSize(1);
        Map<String, Object> summary = result.get(0);
        assertThat(summary.get("total_incidents")).isEqualTo(3);
        assertThat(summary.get("pending_incidents")).isEqualTo(1L);
        assertThat(summary.get("resolved_incidents")).isEqualTo(1L);
    }
}
