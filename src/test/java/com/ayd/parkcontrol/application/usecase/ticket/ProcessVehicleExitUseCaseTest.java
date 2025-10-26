package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.response.ticket.TicketChargeResponse;
import com.ayd.parkcontrol.application.dto.response.ticket.TicketResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessVehicleExitUseCaseTest {

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private JpaTicketChargeRepository ticketChargeRepository;

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @Mock
    private CalculateTicketChargeUseCase calculateTicketChargeUseCase;

    @InjectMocks
    private ProcessVehicleExitUseCase processVehicleExitUseCase;

    private TicketEntity ticket;
    private TicketStatusTypeEntity inProgressStatus;
    private TicketStatusTypeEntity completedStatus;
    private VehicleTypeEntity vehicleType;
    private TicketChargeResponse chargeResponse;

    @BeforeEach
    void setUp() {
        // Status IN_PROGRESS
        inProgressStatus = new TicketStatusTypeEntity();
        inProgressStatus.setId(1);
        inProgressStatus.setCode("IN_PROGRESS");
        inProgressStatus.setName("En Progreso");

        // Status COMPLETED
        completedStatus = new TicketStatusTypeEntity();
        completedStatus.setId(2);
        completedStatus.setCode("COMPLETED");
        completedStatus.setName("Completado");

        // Vehicle type
        vehicleType = new VehicleTypeEntity();
        vehicleType.setId(1);
        vehicleType.setCode("MOTO");
        vehicleType.setName("Motocicleta");

        // Ticket in progress
        ticket = new TicketEntity();
        ticket.setId(1L);
        ticket.setBranchId(1L);
        ticket.setFolio("T-1-12345678");
        ticket.setLicensePlate("ABC-123");
        ticket.setVehicleTypeId(1);
        ticket.setEntryTime(LocalDateTime.now().minusHours(3));
        ticket.setExitTime(null);
        ticket.setStatusTypeId(1);
        ticket.setIsSubscriber(false);
        ticket.setHasIncident(false);
        ticket.setSubscriptionId(null);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        // Charge response
        chargeResponse = TicketChargeResponse.builder()
                .totalHours(BigDecimal.valueOf(3.0))
                .freeHoursGranted(BigDecimal.ZERO)
                .billableHours(BigDecimal.valueOf(3.0))
                .rateApplied(BigDecimal.valueOf(15.0))
                .subtotal(BigDecimal.valueOf(45.0))
                .subscriptionHoursConsumed(BigDecimal.ZERO)
                .subscriptionOverageHours(BigDecimal.ZERO)
                .subscriptionOverageCharge(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(45.0))
                .build();
    }

    @Test
    void execute_ShouldProcessExitSuccessfully_ForNonSubscriber() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(ticketStatusTypeRepository.findByCode("COMPLETED")).thenReturn(Optional.of(completedStatus));
        when(calculateTicketChargeUseCase.execute(1L)).thenReturn(chargeResponse);
        when(ticketChargeRepository.save(any(TicketChargeEntity.class))).thenReturn(new TicketChargeEntity());
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleTypeRepository.findById(1)).thenReturn(Optional.of(vehicleType));

        // Act
        TicketResponse result = processVehicleExitUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals("ABC-123", result.getLicensePlate());
        assertNotNull(result.getExitTime());
        assertEquals(2, result.getStatusTypeId());

        // Verify interactions
        verify(ticketChargeRepository, times(1)).save(any(TicketChargeEntity.class));
        verify(ticketRepository, times(1)).save(any(TicketEntity.class));
        verify(subscriptionRepository, never()).save(any(SubscriptionEntity.class));
    }

    @Test
    void execute_ShouldProcessExitSuccessfully_ForSubscriber() {
        // Arrange - Setup subscriber ticket
        ticket.setIsSubscriber(true);
        ticket.setSubscriptionId(1L);

        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setId(1L);
        subscription.setConsumedHours(BigDecimal.valueOf(5.0));

        chargeResponse = TicketChargeResponse.builder()
                .totalHours(BigDecimal.valueOf(3.0))
                .freeHoursGranted(BigDecimal.ZERO)
                .billableHours(BigDecimal.ZERO)
                .rateApplied(BigDecimal.valueOf(18.0))
                .subtotal(BigDecimal.ZERO)
                .subscriptionHoursConsumed(BigDecimal.valueOf(3.0))
                .subscriptionOverageHours(BigDecimal.ZERO)
                .subscriptionOverageCharge(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(ticketStatusTypeRepository.findByCode("COMPLETED")).thenReturn(Optional.of(completedStatus));
        when(calculateTicketChargeUseCase.execute(1L)).thenReturn(chargeResponse);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(ticketChargeRepository.save(any(TicketChargeEntity.class))).thenReturn(new TicketChargeEntity());
        when(subscriptionRepository.save(any(SubscriptionEntity.class))).thenReturn(subscription);
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleTypeRepository.findById(1)).thenReturn(Optional.of(vehicleType));

        // Act
        TicketResponse result = processVehicleExitUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsSubscriber());
        assertEquals(1L, result.getSubscriptionId());

        // Verify subscription update
        ArgumentCaptor<SubscriptionEntity> subscriptionCaptor = ArgumentCaptor.forClass(SubscriptionEntity.class);
        verify(subscriptionRepository, times(1)).save(subscriptionCaptor.capture());

        SubscriptionEntity savedSubscription = subscriptionCaptor.getValue();
        assertEquals(BigDecimal.valueOf(8.0), savedSubscription.getConsumedHours());
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenTicketNotFound() {
        // Arrange
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> processVehicleExitUseCase.execute(999L));
    }

    @Test
    void execute_ShouldThrowBusinessRuleException_WhenTicketNotInProgress() {
        // Arrange
        ticket.setStatusTypeId(2); // Already completed

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> processVehicleExitUseCase.execute(1L));
    }

    @Test
    void execute_ShouldThrowBusinessRuleException_WhenExitTimeAlreadyRegistered() {
        // Arrange
        ticket.setExitTime(LocalDateTime.now());

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> processVehicleExitUseCase.execute(1L));
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenInProgressStatusNotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> processVehicleExitUseCase.execute(1L));
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenCompletedStatusNotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(calculateTicketChargeUseCase.execute(1L)).thenReturn(chargeResponse);
        when(ticketChargeRepository.save(any(TicketChargeEntity.class))).thenReturn(new TicketChargeEntity());
        when(ticketStatusTypeRepository.findByCode("COMPLETED")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> processVehicleExitUseCase.execute(1L));
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenSubscriptionNotFound() {
        // Arrange - Setup subscriber ticket with non-existent subscription
        ticket.setIsSubscriber(true);
        ticket.setSubscriptionId(999L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(calculateTicketChargeUseCase.execute(1L)).thenReturn(chargeResponse);
        when(ticketChargeRepository.save(any(TicketChargeEntity.class))).thenReturn(new TicketChargeEntity());
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> processVehicleExitUseCase.execute(1L));
    }

    @Test
    void execute_ShouldSaveChargeEntity_WithCorrectValues() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(ticketStatusTypeRepository.findByCode("COMPLETED")).thenReturn(Optional.of(completedStatus));
        when(calculateTicketChargeUseCase.execute(1L)).thenReturn(chargeResponse);
        when(ticketChargeRepository.save(any(TicketChargeEntity.class))).thenReturn(new TicketChargeEntity());
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleTypeRepository.findById(1)).thenReturn(Optional.of(vehicleType));

        // Act
        processVehicleExitUseCase.execute(1L);

        // Assert
        ArgumentCaptor<TicketChargeEntity> chargeCaptor = ArgumentCaptor.forClass(TicketChargeEntity.class);
        verify(ticketChargeRepository).save(chargeCaptor.capture());

        TicketChargeEntity savedCharge = chargeCaptor.getValue();
        assertEquals(1L, savedCharge.getTicketId());
        assertEquals(BigDecimal.valueOf(3.0), savedCharge.getTotalHours());
        assertEquals(BigDecimal.valueOf(45.0), savedCharge.getTotalAmount());
    }
}
