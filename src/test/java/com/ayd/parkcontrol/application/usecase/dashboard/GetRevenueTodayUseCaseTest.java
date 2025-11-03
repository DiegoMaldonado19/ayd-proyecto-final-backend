package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.RevenueTodayResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketChargeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketChargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRevenueTodayUseCaseTest {

    @Mock
    private JpaTicketChargeRepository ticketChargeRepository;

    @Mock
    private JpaSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private GetRevenueTodayUseCase getRevenueTodayUseCase;

    private TicketChargeEntity todayCharge;
    private SubscriptionEntity todaySubscription;

    @BeforeEach
    void setUp() {
        todayCharge = TicketChargeEntity.builder()
                .id(1L)
                .ticketId(1L)
                .totalAmount(BigDecimal.valueOf(50.00))
                .createdAt(LocalDate.now().atTime(14, 0))
                .build();

        todaySubscription = SubscriptionEntity.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .licensePlate("ABC123")
                .frozenRateBase(BigDecimal.valueOf(100.00))
                .purchaseDate(LocalDate.now().atTime(10, 0))
                .startDate(LocalDate.now().atTime(10, 0))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    void execute_shouldReturnRevenueToday_whenDataExists() {
        when(ticketChargeRepository.findAll()).thenReturn(List.of(todayCharge));
        when(subscriptionRepository.findAll()).thenReturn(List.of(todaySubscription));

        RevenueTodayResponse result = getRevenueTodayUseCase.execute();

        assertThat(result).isNotNull();
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        assertThat(result.getTicketsRevenue()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(result.getSubscriptionsRevenue()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(result.getTotalTransactions()).isEqualTo(2L);
    }

    @Test
    void execute_shouldReturnZeroRevenue_whenNoDataExists() {
        when(ticketChargeRepository.findAll()).thenReturn(List.of());
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        RevenueTodayResponse result = getRevenueTodayUseCase.execute();

        assertThat(result).isNotNull();
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTicketsRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getSubscriptionsRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalTransactions()).isEqualTo(0L);
    }

    @Test
    void execute_shouldCalculateAverageTicketValue_whenTicketChargesExist() {
        TicketChargeEntity charge2 = TicketChargeEntity.builder()
                .id(2L)
                .ticketId(2L)
                .totalAmount(BigDecimal.valueOf(30.00))
                .createdAt(LocalDate.now().atTime(15, 0))
                .build();

        when(ticketChargeRepository.findAll()).thenReturn(List.of(todayCharge, charge2));
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        RevenueTodayResponse result = getRevenueTodayUseCase.execute();

        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(80.00));
        assertThat(result.getAverageTicketValue()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
    }

    @Test
    void execute_shouldFilterOldTransactions_whenCalculatingTodayRevenue() {
        TicketChargeEntity yesterdayCharge = TicketChargeEntity.builder()
                .id(2L)
                .ticketId(2L)
                .totalAmount(BigDecimal.valueOf(100.00))
                .createdAt(LocalDate.now().minusDays(1).atTime(14, 0))
                .build();

        when(ticketChargeRepository.findAll()).thenReturn(List.of(todayCharge, yesterdayCharge));
        when(subscriptionRepository.findAll()).thenReturn(List.of(todaySubscription));

        RevenueTodayResponse result = getRevenueTodayUseCase.execute();

        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        assertThat(result.getTotalTransactions()).isEqualTo(2L);
    }
}
