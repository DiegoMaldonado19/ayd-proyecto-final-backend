package com.ayd.parkcontrol.application.usecase.settlement;

import com.ayd.parkcontrol.application.dto.request.settlement.GenerateSettlementRequest;
import com.ayd.parkcontrol.application.dto.response.settlement.SettlementResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateSettlementUseCaseTest {

    @Mock
    private JpaBusinessSettlementHistoryRepository settlementRepository;

    @Mock
    private JpaBusinessFreeHoursRepository businessFreeHoursRepository;

    @Mock
    private JpaAffiliatedBusinessRepository commerceRepository;

    @Mock
    private JpaBranchRepository branchRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaSettlementTicketRepository settlementTicketRepository;

    @Mock
    private JpaTicketRepository ticketRepository;

    @InjectMocks
    private GenerateSettlementUseCase generateSettlementUseCase;

    private GenerateSettlementRequest request;
    private AffiliatedBusinessEntity commerce;
    private BranchEntity branch;
    private UserEntity user;
    private List<BusinessFreeHoursEntity> unsettledHours;
    private BusinessSettlementHistoryEntity settlement;

    @BeforeEach
    void setUp() {
        request = GenerateSettlementRequest.builder()
                .businessId(1L)
                .branchId(1L)
                .periodStart(LocalDateTime.now().minusDays(7))
                .periodEnd(LocalDateTime.now())
                .observations("Monthly settlement")
                .build();

        commerce = AffiliatedBusinessEntity.builder()
                .id(1L)
                .name("Restaurant El Portal")
                .taxId("12345678-9")
                .ratePerHour(BigDecimal.valueOf(8.00))
                .isActive(true)
                .build();

        branch = BranchEntity.builder()
                .id(1L)
                .name("Sucursal Centro")
                .build();

        user = UserEntity.builder()
                .id(1L)
                .email("admin@parkcontrol.com")
                .firstName("Carlos")
                .lastName("Rodriguez")
                .build();

        BusinessFreeHoursEntity freeHours1 = BusinessFreeHoursEntity.builder()
                .id(1L)
                .ticketId(1L)
                .businessId(1L)
                .branchId(1L)
                .grantedHours(BigDecimal.valueOf(2.0))
                .grantedAt(LocalDateTime.now().minusDays(3))
                .isSettled(false)
                .build();

        BusinessFreeHoursEntity freeHours2 = BusinessFreeHoursEntity.builder()
                .id(2L)
                .ticketId(2L)
                .businessId(1L)
                .branchId(1L)
                .grantedHours(BigDecimal.valueOf(3.0))
                .grantedAt(LocalDateTime.now().minusDays(1))
                .isSettled(false)
                .build();

        unsettledHours = Arrays.asList(freeHours1, freeHours2);

        settlement = BusinessSettlementHistoryEntity.builder()
                .id(1L)
                .businessId(1L)
                .branchId(1L)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .totalHours(BigDecimal.valueOf(5.0))
                .totalAmount(BigDecimal.valueOf(40.0))
                .ticketCount(2)
                .settledAt(LocalDateTime.now())
                .settledBy(1L)
                .observations("Monthly settlement")
                .createdAt(LocalDateTime.now())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@parkcontrol.com", "password"));
    }

    @Test
    void execute_ShouldGenerateSettlement_WhenDataIsValid() {
        when(commerceRepository.findById(1L)).thenReturn(Optional.of(commerce));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(userRepository.findByEmail("admin@parkcontrol.com")).thenReturn(Optional.of(user));
        when(businessFreeHoursRepository.findUnsettledByBusinessBranchAndPeriod(
                any(), any(), any(), any())).thenReturn(unsettledHours);
        when(settlementRepository.save(any(BusinessSettlementHistoryEntity.class))).thenReturn(settlement);
        when(settlementTicketRepository.save(any(SettlementTicketEntity.class)))
                .thenReturn(SettlementTicketEntity.builder().build());
        when(ticketRepository.findById(any())).thenReturn(Optional.of(TicketEntity.builder()
                .folio("T-1-12345678")
                .licensePlate("ABC-123")
                .build()));

        SettlementResponse response = generateSettlementUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBusinessName()).isEqualTo("Restaurant El Portal");
        assertThat(response.getTotalHours()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(40.0));
        assertThat(response.getTicketCount()).isEqualTo(2);

        verify(settlementRepository).save(any(BusinessSettlementHistoryEntity.class));
        verify(settlementTicketRepository, times(2)).save(any(SettlementTicketEntity.class));
        verify(businessFreeHoursRepository).markAsSettled(anyList());
    }

    @Test
    void execute_ShouldThrowBusinessRuleException_WhenPeriodEndBeforeStart() {
        request.setPeriodEnd(request.getPeriodStart().minusDays(1));

        assertThatThrownBy(() -> generateSettlementUseCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("after period start");

        verify(settlementRepository, never()).save(any());
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenCommerceNotFound() {
        when(commerceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generateSettlementUseCase.execute(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Commerce not found");

        verify(settlementRepository, never()).save(any());
    }

    @Test
    void execute_ShouldThrowBusinessRuleException_WhenNoUnsettledHours() {
        when(commerceRepository.findById(1L)).thenReturn(Optional.of(commerce));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(userRepository.findByEmail("admin@parkcontrol.com")).thenReturn(Optional.of(user));
        when(businessFreeHoursRepository.findUnsettledByBusinessBranchAndPeriod(
                any(), any(), any(), any())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> generateSettlementUseCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No unsettled free hours found");

        verify(settlementRepository, never()).save(any());
    }
}
