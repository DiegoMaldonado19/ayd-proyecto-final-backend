package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.SystemAlertResponse;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.IncidentEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaIncidentRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSystemAlertsUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private JpaIncidentRepository incidentRepository;

    @InjectMocks
    private GetSystemAlertsUseCase getSystemAlertsUseCase;

    private Branch branch;
    private TicketStatusTypeEntity inProgressStatus;
    private VehicleTypeEntity vehicleType2R;
    private VehicleTypeEntity vehicleType4R;
    private IncidentEntity pendingIncident;

    @BeforeEach
    void setUp() {
        branch = Branch.builder()
                .id(1L)
                .name("Test Branch")
                .capacity2r(10)
                .capacity4r(10)
                .build();

        inProgressStatus = TicketStatusTypeEntity.builder()
                .id(1)
                .code("IN_PROGRESS")
                .build();

        vehicleType2R = VehicleTypeEntity.builder()
                .id(1)
                .code("2R")
                .build();

        vehicleType4R = VehicleTypeEntity.builder()
                .id(2)
                .code("4R")
                .build();

        pendingIncident = IncidentEntity.builder()
                .id(1L)
                .ticketId(1L)
                .incidentTypeId(1)
                .isResolved(false)
                .build();
    }

    @Test
    void execute_shouldReturnCriticalOccupancyAlert_when90PercentOccupied() {
        Page<Branch> branches = new PageImpl<>(List.of(branch));
        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branches);
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(vehicleTypeRepository.findByCode("2R")).thenReturn(Optional.of(vehicleType2R));
        when(vehicleTypeRepository.findByCode("4R")).thenReturn(Optional.of(vehicleType4R));
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(anyLong(), anyInt(), anyInt()))
                .thenReturn(9L).thenReturn(9L);
        when(incidentRepository.findAll()).thenReturn(List.of());

        List<SystemAlertResponse> result = getSystemAlertsUseCase.execute();

        assertThat(result).hasSizeGreaterThan(0);
        assertThat(result.get(0).getAlertType()).isEqualTo("CRITICAL_OCCUPANCY");
        assertThat(result.get(0).getSeverity()).isEqualTo("HIGH");
    }

    @Test
    void execute_shouldReturnHighOccupancyAlert_when80PercentOccupied() {
        Page<Branch> branches = new PageImpl<>(List.of(branch));
        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branches);
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(vehicleTypeRepository.findByCode("2R")).thenReturn(Optional.of(vehicleType2R));
        when(vehicleTypeRepository.findByCode("4R")).thenReturn(Optional.of(vehicleType4R));
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(anyLong(), anyInt(), anyInt()))
                .thenReturn(8L).thenReturn(8L);
        when(incidentRepository.findAll()).thenReturn(List.of());

        List<SystemAlertResponse> result = getSystemAlertsUseCase.execute();

        assertThat(result).hasSizeGreaterThan(0);
        assertThat(result.get(0).getAlertType()).isEqualTo("HIGH_OCCUPANCY");
        assertThat(result.get(0).getSeverity()).isEqualTo("MEDIUM");
    }

    @Test
    void execute_shouldReturnPendingIncidentsAlert_whenUnresolvedIncidentsExist() {
        Page<Branch> branches = new PageImpl<>(List.of(branch));
        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branches);
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(vehicleTypeRepository.findByCode("2R")).thenReturn(Optional.of(vehicleType2R));
        when(vehicleTypeRepository.findByCode("4R")).thenReturn(Optional.of(vehicleType4R));
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(anyLong(), anyInt(), anyInt()))
                .thenReturn(5L).thenReturn(5L);
        when(incidentRepository.findAll()).thenReturn(List.of(pendingIncident));

        List<SystemAlertResponse> result = getSystemAlertsUseCase.execute();

        assertThat(result).hasSizeGreaterThan(0);
        boolean hasPendingIncidentAlert = result.stream()
                .anyMatch(alert -> alert.getAlertType().equals("PENDING_INCIDENTS"));
        assertThat(hasPendingIncidentAlert).isTrue();
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoAlertsExist() {
        Page<Branch> branches = new PageImpl<>(List.of(branch));
        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branches);
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(vehicleTypeRepository.findByCode("2R")).thenReturn(Optional.of(vehicleType2R));
        when(vehicleTypeRepository.findByCode("4R")).thenReturn(Optional.of(vehicleType4R));
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(anyLong(), anyInt(), anyInt()))
                .thenReturn(2L).thenReturn(2L);
        when(incidentRepository.findAll()).thenReturn(List.of());

        List<SystemAlertResponse> result = getSystemAlertsUseCase.execute();

        assertThat(result).isEmpty();
    }
}
