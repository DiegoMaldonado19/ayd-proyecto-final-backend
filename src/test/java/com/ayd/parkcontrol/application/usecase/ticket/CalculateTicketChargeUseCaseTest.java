package com.ayd.parkcontrol.application.usecase.ticket;

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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculateTicketChargeUseCaseTest {

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaBranchRepository branchRepository;

    @Mock
    private JpaRateBaseHistoryRepository rateBaseHistoryRepository;

    @Mock
    private JpaBusinessFreeHoursRepository businessFreeHoursRepository;

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private CalculateTicketChargeUseCase calculateTicketChargeUseCase;

    private TicketEntity ticket;
    private BranchEntity branch;
    private RateBaseHistoryEntity rateBase;
    private SubscriptionEntity subscription;
    private SubscriptionPlanEntity subscriptionPlan;

    @BeforeEach
    void setUp() {
        // Setup branch
        branch = new BranchEntity();
        branch.setId(1L);
        branch.setName("Sucursal Centro");
        branch.setRatePerHour(BigDecimal.valueOf(15.00));

        // Setup rate base
        rateBase = new RateBaseHistoryEntity();
        rateBase.setId(1L);
        rateBase.setAmountPerHour(BigDecimal.valueOf(20.00));
        rateBase.setIsActive(true);

        // Setup subscription plan
        subscriptionPlan = new SubscriptionPlanEntity();
        subscriptionPlan.setId(1L);
        subscriptionPlan.setMonthlyHours(10);

        // Setup subscription
        subscription = new SubscriptionEntity();
        subscription.setId(1L);
        subscription.setFrozenRateBase(BigDecimal.valueOf(18.00));
        subscription.setConsumedHours(BigDecimal.valueOf(5.0));
        subscription.setPlanId(1L);
        subscription.setPlan(subscriptionPlan);

        // Setup ticket
        ticket = new TicketEntity();
        ticket.setId(1L);
        ticket.setBranchId(1L);
        ticket.setLicensePlate("ABC-123");
        ticket.setIsSubscriber(false);
        ticket.setEntryTime(LocalDateTime.now().minusHours(3));
    }

    @Test
    void calculateCharge_ShouldThrowNotFoundException_WhenTicketNotFound() {
        // Arrange
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> calculateTicketChargeUseCase.execute(999L));
    }

    @Test
    void calculateCharge_ShouldCalculateCorrectly_ForNonSubscriberWithoutFreeHours() {
        // Arrange
        ticket.setExitTime(LocalDateTime.now());
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(businessFreeHoursRepository.sumGrantedHoursByTicketId(1L)).thenReturn(BigDecimal.ZERO);

        // Act
        var result = calculateTicketChargeUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalHours().intValue());
        assertEquals(BigDecimal.ZERO, result.getFreeHoursGranted());
        assertTrue(result.getBillableHours().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(branch.getRatePerHour(), result.getRateApplied());
        assertEquals(BigDecimal.ZERO, result.getSubscriptionHoursConsumed());
        assertEquals(BigDecimal.ZERO, result.getSubscriptionOverageHours());
        assertTrue(result.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateCharge_ShouldCalculateCorrectly_ForNonSubscriberWithFreeHours() {
        // Arrange
        ticket.setExitTime(LocalDateTime.now());
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(businessFreeHoursRepository.sumGrantedHoursByTicketId(1L)).thenReturn(BigDecimal.valueOf(1.5));

        // Act
        var result = calculateTicketChargeUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalHours().intValue());
        assertEquals(BigDecimal.valueOf(1.5), result.getFreeHoursGranted());
        assertTrue(result.getBillableHours().compareTo(BigDecimal.valueOf(1.5)) >= 0);
        assertEquals(BigDecimal.ZERO, result.getSubscriptionHoursConsumed());
    }

    @Test
    void calculateCharge_ShouldCalculateCorrectly_ForSubscriberWithoutFreeHours() {
        // Arrange
        ticket.setIsSubscriber(true);
        ticket.setSubscriptionId(1L);
        ticket.setExitTime(LocalDateTime.now());
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(businessFreeHoursRepository.sumGrantedHoursByTicketId(1L)).thenReturn(BigDecimal.ZERO);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        // Act
        var result = calculateTicketChargeUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalHours().intValue());
        assertEquals(BigDecimal.ZERO, result.getFreeHoursGranted());
        assertNotNull(result.getSubscriptionHoursConsumed());
        assertEquals(subscription.getFrozenRateBase(), result.getRateApplied());
    }

    @Test
    void calculateCharge_ShouldCalculateCorrectly_ForSubscriberWithFreeHours() {
        // Arrange
        ticket.setIsSubscriber(true);
        ticket.setSubscriptionId(1L);
        ticket.setExitTime(LocalDateTime.now());
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(businessFreeHoursRepository.sumGrantedHoursByTicketId(1L)).thenReturn(BigDecimal.valueOf(1.0));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        // Act
        var result = calculateTicketChargeUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalHours().intValue());
        assertEquals(BigDecimal.valueOf(1.0), result.getFreeHoursGranted());
        // Free hours should not affect subscription hours consumed
        assertNotNull(result.getSubscriptionHoursConsumed());
        assertTrue(result.getSubscriptionHoursConsumed().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateCharge_ShouldCalculateOverage_ForSubscriberExceedingMonthlyHours() {
        // Arrange
        subscription.setConsumedHours(BigDecimal.valueOf(9.0)); // Almost at limit
        ticket.setIsSubscriber(true);
        ticket.setSubscriptionId(1L);
        ticket.setExitTime(LocalDateTime.now()); // 3 hours total
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(businessFreeHoursRepository.sumGrantedHoursByTicketId(1L)).thenReturn(BigDecimal.ZERO);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        // Act
        var result = calculateTicketChargeUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalHours().intValue());
        assertNotNull(result.getSubscriptionOverageHours());
        assertTrue(result.getSubscriptionOverageHours().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(result.getSubscriptionOverageCharge());
        assertTrue(result.getSubscriptionOverageCharge().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateCharge_ShouldUseBranchRate_WhenNoRateBaseExists() {
        // Arrange
        ticket.setExitTime(LocalDateTime.now());
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(businessFreeHoursRepository.sumGrantedHoursByTicketId(1L)).thenReturn(BigDecimal.ZERO);

        // Act
        var result = calculateTicketChargeUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(branch.getRatePerHour(), result.getRateApplied());
    }

    @Test
    void calculateCharge_ShouldThrowException_WhenBranchNotFound() {
        // Arrange
        ticket.setExitTime(LocalDateTime.now());
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> calculateTicketChargeUseCase.execute(1L));
    }
}
