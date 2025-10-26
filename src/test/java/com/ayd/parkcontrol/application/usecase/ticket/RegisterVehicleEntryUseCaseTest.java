package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.request.ticket.RegisterEntryRequest;
import com.ayd.parkcontrol.application.dto.response.ticket.TicketResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.InsufficientCapacityException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterVehicleEntryUseCaseTest {

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaBranchRepository branchRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @InjectMocks
    private RegisterVehicleEntryUseCase registerVehicleEntryUseCase;

    private RegisterEntryRequest request;
    private BranchEntity branch;
    private VehicleTypeEntity vehicleType;
    private TicketStatusTypeEntity inProgressStatus;

    @BeforeEach
    void setUp() {
        request = RegisterEntryRequest.builder()
                .branchId(1L)
                .licensePlate("ABC-123")
                .vehicleTypeId(2)
                .build();

        branch = BranchEntity.builder()
                .id(1L)
                .name("Branch Centro")
                .address("Address 1")
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(20, 0))
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(BigDecimal.valueOf(10.00))
                .isActive(true)
                .build();

        vehicleType = VehicleTypeEntity.builder()
                .id(2)
                .code("4R")
                .name("Cuatro Ruedas")
                .build();

        inProgressStatus = TicketStatusTypeEntity.builder()
                .id(1)
                .code("IN_PROGRESS")
                .name("En Curso")
                .build();
    }

    @Test
    void execute_ShouldRegisterEntry_WhenDataIsValid() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(vehicleTypeRepository.findById(2)).thenReturn(Optional.of(vehicleType));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(1L, 2, 1)).thenReturn(50L);
        when(ticketRepository.existsActiveTicketForPlateInBranch(anyString(), eq(1L), eq(1))).thenReturn(false);
        when(subscriptionRepository.findActiveLicensePlateSubscription(anyString())).thenReturn(Optional.empty());

        TicketEntity savedTicket = TicketEntity.builder()
                .id(1L)
                .branchId(1L)
                .folio("T-1-12345678")
                .licensePlate("ABC-123")
                .vehicleTypeId(2)
                .subscriptionId(null)
                .isSubscriber(false)
                .hasIncident(false)
                .statusTypeId(1)
                .qrCode("some-qr-code")
                .build();

        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(savedTicket);

        // Act
        TicketResponse response = registerVehicleEntryUseCase.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLicensePlate()).isEqualTo("ABC-123");
        assertThat(response.getIsSubscriber()).isFalse();

        verify(ticketRepository, times(1)).save(any(TicketEntity.class));
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenBranchNotFound() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> registerVehicleEntryUseCase.execute(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Branch not found");
    }

    @Test
    void execute_ShouldThrowBusinessRuleException_WhenBranchIsNotActive() {
        // Arrange
        branch.setIsActive(false);
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        // Act & Assert
        assertThatThrownBy(() -> registerVehicleEntryUseCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Branch is not active");
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenVehicleTypeNotFound() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(vehicleTypeRepository.findById(2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> registerVehicleEntryUseCase.execute(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Vehicle type not found");
    }

    @Test
    void execute_ShouldThrowInsufficientCapacityException_WhenBranchIsFull() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(vehicleTypeRepository.findById(2)).thenReturn(Optional.of(vehicleType));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(1L, 2, 1)).thenReturn(100L);

        // Act & Assert
        assertThatThrownBy(() -> registerVehicleEntryUseCase.execute(request))
                .isInstanceOf(InsufficientCapacityException.class)
                .hasMessageContaining("Insufficient capacity");
    }

    @Test
    void execute_ShouldThrowBusinessRuleException_WhenVehicleAlreadyHasActiveTicket() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(vehicleTypeRepository.findById(2)).thenReturn(Optional.of(vehicleType));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(1L, 2, 1)).thenReturn(50L);
        when(ticketRepository.existsActiveTicketForPlateInBranch(anyString(), eq(1L), eq(1))).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registerVehicleEntryUseCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already has an active ticket");
    }

    @Test
    void execute_ShouldRegisterEntryWithSubscription_WhenVehicleHasActiveSubscription() {
        // Arrange
        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .build();

        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(vehicleTypeRepository.findById(2)).thenReturn(Optional.of(vehicleType));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(ticketRepository.countActiveTicketsByBranchAndVehicleType(1L, 2, 1)).thenReturn(50L);
        when(ticketRepository.existsActiveTicketForPlateInBranch(anyString(), eq(1L), eq(1))).thenReturn(false);
        when(subscriptionRepository.findActiveLicensePlateSubscription(anyString()))
                .thenReturn(Optional.of(subscription));

        TicketEntity savedTicket = TicketEntity.builder()
                .id(1L)
                .branchId(1L)
                .folio("T-1-12345678")
                .licensePlate("ABC-123")
                .vehicleTypeId(2)
                .subscriptionId(1L)
                .isSubscriber(true)
                .hasIncident(false)
                .statusTypeId(1)
                .qrCode("some-qr-code")
                .build();

        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(savedTicket);

        // Act
        TicketResponse response = registerVehicleEntryUseCase.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsSubscriber()).isTrue();
        assertThat(response.getSubscriptionId()).isEqualTo(1L);

        verify(ticketRepository, times(1)).save(any(TicketEntity.class));
    }
}
