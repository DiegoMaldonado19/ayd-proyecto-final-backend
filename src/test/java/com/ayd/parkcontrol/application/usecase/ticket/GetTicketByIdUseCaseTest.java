package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.response.ticket.TicketResponse;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTicketByIdUseCaseTest {

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @InjectMocks
    private GetTicketByIdUseCase getTicketByIdUseCase;

    private TicketEntity ticket;
    private VehicleTypeEntity vehicleType;
    private TicketStatusTypeEntity status;

    @BeforeEach
    void setUp() {
        ticket = TicketEntity.builder()
                .id(1L)
                .branchId(1L)
                .folio("T-1-12345678")
                .licensePlate("ABC-123")
                .vehicleTypeId(2)
                .entryTime(LocalDateTime.now())
                .subscriptionId(null)
                .isSubscriber(false)
                .hasIncident(false)
                .statusTypeId(1)
                .qrCode("qr-code")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        vehicleType = VehicleTypeEntity.builder()
                .id(2)
                .code("4R")
                .name("Cuatro Ruedas")
                .build();

        status = TicketStatusTypeEntity.builder()
                .id(1)
                .code("IN_PROGRESS")
                .name("En Curso")
                .build();
    }

    @Test
    void execute_ShouldReturnTicket_WhenTicketExists() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(vehicleTypeRepository.findById(2)).thenReturn(Optional.of(vehicleType));
        when(ticketStatusTypeRepository.findById(1)).thenReturn(Optional.of(status));

        // Act
        TicketResponse response = getTicketByIdUseCase.execute(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFolio()).isEqualTo("T-1-12345678");
        assertThat(response.getLicensePlate()).isEqualTo("ABC-123");
        assertThat(response.getVehicleTypeName()).isEqualTo("Cuatro Ruedas");
        assertThat(response.getStatusName()).isEqualTo("En Curso");
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenTicketNotFound() {
        // Arrange
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getTicketByIdUseCase.execute(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenVehicleTypeNotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(vehicleTypeRepository.findById(2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getTicketByIdUseCase.execute(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Vehicle type not found");
    }
}
