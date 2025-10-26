package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.response.ticket.TicketResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTicketsByBranchUseCaseTest {

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @InjectMocks
    private GetTicketsByBranchUseCase getTicketsByBranchUseCase;

    private TicketEntity ticket1;
    private TicketEntity ticket2;
    private VehicleTypeEntity vehicleType;
    private TicketStatusTypeEntity statusType;

    @BeforeEach
    void setUp() {
        vehicleType = new VehicleTypeEntity();
        vehicleType.setId(1);
        vehicleType.setCode("MOTO");
        vehicleType.setName("Motocicleta");

        statusType = new TicketStatusTypeEntity();
        statusType.setId(1);
        statusType.setCode("IN_PROGRESS");
        statusType.setName("En Progreso");

        ticket1 = new TicketEntity();
        ticket1.setId(1L);
        ticket1.setBranchId(1L);
        ticket1.setLicensePlate("ABC-123");
        ticket1.setVehicleTypeId(1);
        ticket1.setStatusTypeId(1);
        ticket1.setIsSubscriber(false);
        ticket1.setEntryTime(LocalDateTime.now());
        ticket1.setFolio("T-1-001");

        ticket2 = new TicketEntity();
        ticket2.setId(2L);
        ticket2.setBranchId(1L);
        ticket2.setLicensePlate("DEF-456");
        ticket2.setVehicleTypeId(2);
        ticket2.setStatusTypeId(1);
        ticket2.setIsSubscriber(true);
        ticket2.setEntryTime(LocalDateTime.now());
        ticket2.setFolio("T-1-002");
    }

    @Test
    void execute_ShouldReturnTickets_WhenTicketsExist() {
        // Arrange
        when(ticketRepository.findByBranchIdOrderByEntryTimeDesc(1L)).thenReturn(Arrays.asList(ticket1, ticket2));
        when(vehicleTypeRepository.findAll()).thenReturn(List.of(vehicleType));
        when(ticketStatusTypeRepository.findAll()).thenReturn(List.of(statusType));

        // Act
        List<TicketResponse> result = getTicketsByBranchUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void execute_ShouldReturnEmptyList_WhenNoTicketsExist() {
        // Arrange
        when(ticketRepository.findByBranchIdOrderByEntryTimeDesc(999L)).thenReturn(Collections.emptyList());
        when(vehicleTypeRepository.findAll()).thenReturn(List.of(vehicleType));
        when(ticketStatusTypeRepository.findAll()).thenReturn(List.of(statusType));

        // Act
        List<TicketResponse> result = getTicketsByBranchUseCase.execute(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
