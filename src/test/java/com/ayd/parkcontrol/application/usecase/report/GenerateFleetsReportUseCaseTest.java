package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateFleetsReportUseCaseTest {

    @Mock
    private JpaTicketRepository ticketRepository;

    @InjectMocks
    private GenerateFleetsReportUseCase generateFleetsReportUseCase;

    private TicketEntity ticket1;
    private TicketEntity ticket2;
    private TicketEntity ticket3;

    @BeforeEach
    void setUp() {
        ticket1 = TicketEntity.builder()
                .id(1L)
                .licensePlate("ABC123")
                .build();

        ticket2 = TicketEntity.builder()
                .id(2L)
                .licensePlate("ABC123")
                .build();

        ticket3 = TicketEntity.builder()
                .id(3L)
                .licensePlate("ABC123")
                .build();
    }

    @Test
    void execute_shouldGenerateFleetsReport_whenPlatesHaveMultipleEntries() {
        when(ticketRepository.findAll()).thenReturn(List.of(ticket1, ticket2, ticket3));
        when(ticketRepository.sumHoursByLicensePlate(anyString())).thenReturn(BigDecimal.valueOf(15.0));
        when(ticketRepository.sumAmountByLicensePlate(anyString())).thenReturn(BigDecimal.valueOf(100.0));

        List<Map<String, Object>> result = generateFleetsReportUseCase.execute();

        assertThat(result).hasSize(1);
        Map<String, Object> fleetData = result.get(0);
        assertThat(fleetData.get("license_plate")).isEqualTo("ABC123");
        assertThat(fleetData.get("total_entries")).isEqualTo(3L);
        assertThat(fleetData.get("total_hours")).isEqualTo(BigDecimal.valueOf(15.0));
        assertThat(fleetData.get("total_amount")).isEqualTo(BigDecimal.valueOf(100.0));
    }

    @Test
    void execute_shouldExcludePlatesWithFewerThan3Entries_whenFilteringFleets() {
        TicketEntity singleEntry = TicketEntity.builder()
                .id(4L)
                .licensePlate("XYZ789")
                .build();

        when(ticketRepository.findAll()).thenReturn(List.of(ticket1, ticket2, ticket3, singleEntry));
        when(ticketRepository.sumHoursByLicensePlate("ABC123")).thenReturn(BigDecimal.valueOf(15.0));
        when(ticketRepository.sumAmountByLicensePlate("ABC123")).thenReturn(BigDecimal.valueOf(100.0));

        List<Map<String, Object>> result = generateFleetsReportUseCase.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("license_plate")).isEqualTo("ABC123");
    }

    @Test
    void execute_shouldReturnEmptyReport_whenNoFleetsExist() {
        when(ticketRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = generateFleetsReportUseCase.execute();

        assertThat(result).isEmpty();
    }
}
