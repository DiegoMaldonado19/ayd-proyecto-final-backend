package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.DashboardOverviewResponse;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketChargeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketChargeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDashboardByBranchUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaTicketChargeRepository ticketChargeRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Mock
    private JpaIncidentRepository incidentRepository;

    @InjectMocks
    private GetDashboardByBranchUseCase getDashboardByBranchUseCase;

    private Branch branch;
    private TicketStatusTypeEntity inProgressStatus;
    private TicketEntity activeTicket;
    private TicketChargeEntity todayCharge;

    @BeforeEach
    void setUp() {
        branch = Branch.builder()
                .id(1L)
                .name("Test Branch")
                .capacity2r(50)
                .capacity4r(30)
                .build();

        inProgressStatus = TicketStatusTypeEntity.builder()
                .id(1)
                .code("IN_PROGRESS")
                .name("In Progress")
                .build();

        activeTicket = TicketEntity.builder()
                .id(1L)
                .branchId(1L)
                .statusTypeId(1)
                .entryTime(LocalDate.now().atTime(10, 0))
                .build();

        todayCharge = TicketChargeEntity.builder()
                .id(1L)
                .ticketId(1L)
                .totalAmount(BigDecimal.valueOf(50.00))
                .createdAt(LocalDate.now().atTime(14, 0))
                .build();
    }

    @Test
    void execute_shouldReturnBranchDashboard_whenBranchExists() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(ticketRepository.findByBranchIdAndStatusTypeId(anyLong(), anyInt())).thenReturn(List.of(activeTicket));
        when(ticketRepository.findByBranchIdOrderByEntryTimeDesc(anyLong())).thenReturn(List.of(activeTicket));
        when(ticketChargeRepository.findAll()).thenReturn(List.of(todayCharge));

        DashboardOverviewResponse result = getDashboardByBranchUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getActiveTickets()).isEqualTo(1L);
        assertThat(result.getTotalVehiclesToday()).isEqualTo(1L);
        assertThat(result.getRevenueToday()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    @Test
    void execute_shouldThrowException_whenBranchNotFound() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getDashboardByBranchUseCase.execute(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Sucursal no encontrada");
    }

    @Test
    void execute_shouldCalculateActiveTickets_whenMultipleTicketsExist() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(ticketRepository.findByBranchIdAndStatusTypeId(anyLong(), anyInt()))
                .thenReturn(List.of(activeTicket, activeTicket, activeTicket));
        when(ticketRepository.findByBranchIdOrderByEntryTimeDesc(anyLong())).thenReturn(List.of());
        when(ticketChargeRepository.findAll()).thenReturn(List.of());

        DashboardOverviewResponse result = getDashboardByBranchUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getActiveTickets()).isEqualTo(3L);
    }
}
