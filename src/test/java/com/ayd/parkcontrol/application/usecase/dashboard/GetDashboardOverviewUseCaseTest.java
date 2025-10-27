package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDashboardOverviewUseCase Tests")
class GetDashboardOverviewUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private PlateChangeRequestRepository plateChangeRequestRepository;

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @Mock
    private JpaTicketChargeRepository ticketChargeRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Mock
    private JpaIncidentRepository incidentRepository;

    @InjectMocks
    private GetDashboardOverviewUseCase getDashboardOverviewUseCase;

    private TicketStatusTypeEntity inProgressStatus;
    private Branch branch;
    private TicketEntity ticket;
    private SubscriptionEntity subscription;
    private TicketChargeEntity charge;
    private IncidentEntity incident;

    @BeforeEach
    void setUp() {
        // Status IN_PROGRESS
        inProgressStatus = new TicketStatusTypeEntity();
        inProgressStatus.setId(1);
        inProgressStatus.setCode("IN_PROGRESS");
        inProgressStatus.setName("En Curso");

        // Branch
        branch = Branch.builder()
                .id(1L)
                .name("Sucursal Centro")
                .capacity2r(50)
                .capacity4r(30)
                .ratePerHour(BigDecimal.valueOf(20.00))
                .isActive(true)
                .build();

        // Ticket
        ticket = new TicketEntity();
        ticket.setId(1L);
        ticket.setBranchId(1L);
        ticket.setFolio("T-001");
        ticket.setLicensePlate("ABC-123");
        ticket.setStatusTypeId(1);
        ticket.setEntryTime(LocalDateTime.now().minusHours(2));

        // Subscription
        subscription = new SubscriptionEntity();
        subscription.setId(1L);
        subscription.setUserId(1L);
        subscription.setPlanId(1L);
        subscription.setLicensePlate("XYZ-789");
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        subscription.setFrozenRateBase(BigDecimal.valueOf(500.00));

        // Ticket Charge
        charge = new TicketChargeEntity();
        charge.setId(1L);
        charge.setTicketId(1L);
        charge.setTotalAmount(BigDecimal.valueOf(40.00));
        charge.setCreatedAt(LocalDateTime.now());

        // Incident
        incident = new IncidentEntity();
        incident.setId(1L);
        incident.setBranchId(1L);
        incident.setIsResolved(false);
        incident.setDescription("Incidente de prueba");
    }

    @Test
    @DisplayName("Debe obtener el resumen del dashboard exitosamente")
    void shouldGetDashboardOverviewSuccessfully() {
        // Arrange
        when(branchRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(branch)));
        when(plateChangeRequestRepository.findAll())
                .thenReturn(List.of());
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.of(inProgressStatus));
        when(ticketRepository.findByStatusTypeId(anyInt()))
                .thenReturn(List.of(ticket));
        when(subscriptionRepository.findAll())
                .thenReturn(List.of(subscription));
        when(ticketRepository.findAll())
                .thenReturn(List.of(ticket));
        when(ticketChargeRepository.findAll())
                .thenReturn(List.of(charge));
        when(ticketRepository.findByBranchIdAndStatusTypeId(anyLong(), anyInt()))
                .thenReturn(List.of(ticket));
        when(incidentRepository.findAll())
                .thenReturn(List.of(incident));

        // Act
        var result = getDashboardOverviewUseCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalBranches());
        assertEquals(1L, result.getActiveTickets());
        assertEquals(1L, result.getActiveSubscriptions());
        assertTrue(result.getRevenueToday().compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(1L, result.getPendingIncidents());

        verify(ticketStatusTypeRepository).findByCode("IN_PROGRESS");
        verify(subscriptionRepository).findAll();
    }

    @Test
    @DisplayName("Debe calcular ocupación promedio correctamente")
    void shouldCalculateAverageOccupancyCorrectly() {
        // Arrange
        Branch branch1 = Branch.builder()
                .id(1L)
                .name("Sucursal 1")
                .capacity2r(50)
                .capacity4r(50)
                .build();

        Branch branch2 = Branch.builder()
                .id(2L)
                .name("Sucursal 2")
                .capacity2r(40)
                .capacity4r(60)
                .build();

        when(branchRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(branch1, branch2)));
        when(plateChangeRequestRepository.findAll()).thenReturn(List.of());
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.of(inProgressStatus));
        when(ticketRepository.findByStatusTypeId(anyInt())).thenReturn(List.of());
        when(subscriptionRepository.findAll()).thenReturn(List.of());
        when(ticketRepository.findAll()).thenReturn(List.of());
        when(ticketChargeRepository.findAll()).thenReturn(List.of());
        when(ticketRepository.findByBranchIdAndStatusTypeId(1L, 1)).thenReturn(List.of(ticket));
        when(ticketRepository.findByBranchIdAndStatusTypeId(2L, 1)).thenReturn(List.of());
        when(incidentRepository.findAll()).thenReturn(List.of());

        // Act
        var result = getDashboardOverviewUseCase.execute();

        // Assert
        assertNotNull(result);
        assertTrue(result.getAverageOccupancyPercentage() >= 0);
        assertTrue(result.getAverageOccupancyPercentage() <= 100);
    }

    @Test
    @DisplayName("Debe manejar correctamente cuando no hay datos")
    void shouldHandleNoDataGracefully() {
        // Arrange
        when(branchRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(plateChangeRequestRepository.findAll()).thenReturn(List.of());
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.of(inProgressStatus));
        when(ticketRepository.findByStatusTypeId(anyInt())).thenReturn(List.of());
        when(subscriptionRepository.findAll()).thenReturn(List.of());
        when(ticketRepository.findAll()).thenReturn(List.of());
        when(ticketChargeRepository.findAll()).thenReturn(List.of());
        when(incidentRepository.findAll()).thenReturn(List.of());

        // Act
        var result = getDashboardOverviewUseCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalBranches());
        assertEquals(0L, result.getActiveTickets());
        assertEquals(0L, result.getActiveSubscriptions());
        assertEquals(BigDecimal.ZERO, result.getRevenueToday());
        assertEquals(0.0, result.getAverageOccupancyPercentage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el estado IN_PROGRESS no existe")
    void shouldThrowExceptionWhenInProgressStatusNotFound() {
        // Arrange
        when(branchRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(branch)));
        when(plateChangeRequestRepository.findAll()).thenReturn(List.of());
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> getDashboardOverviewUseCase.execute());
        verify(ticketStatusTypeRepository).findByCode("IN_PROGRESS");
    }
}
