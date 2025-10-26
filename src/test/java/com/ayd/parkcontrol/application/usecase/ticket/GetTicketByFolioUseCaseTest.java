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
class GetTicketByFolioUseCaseTest {

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @InjectMocks
    private GetTicketByFolioUseCase getTicketByFolioUseCase;

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
    void execute_ShouldReturnTicket_WhenFolioExists() {
        // Arrange
        when(ticketRepository.findByFolio("T-1-12345678")).thenReturn(Optional.of(ticket));
        when(vehicleTypeRepository.findById(2)).thenReturn(Optional.of(vehicleType));
        when(ticketStatusTypeRepository.findById(1)).thenReturn(Optional.of(status));

        // Act
        TicketResponse response = getTicketByFolioUseCase.execute("T-1-12345678");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getFolio()).isEqualTo("T-1-12345678");
        assertThat(response.getLicensePlate()).isEqualTo("ABC-123");
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenFolioNotFound() {
        // Arrange
        when(ticketRepository.findByFolio("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getTicketByFolioUseCase.execute("INVALID"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket not found with folio");
    }
}
