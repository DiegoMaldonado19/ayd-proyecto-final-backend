package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketChargeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketChargeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCashClosingReportUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaTicketChargeRepository ticketChargeRepository;

    @InjectMocks
    private GenerateCashClosingReportUseCase generateCashClosingReportUseCase;

    private Branch branch;
    private TicketEntity ticket;
    private TicketChargeEntity charge;

    @BeforeEach
    void setUp() {
        branch = Branch.builder()
                .id(1L)
                .name("Test Branch")
                .build();

        LocalDateTime now = LocalDate.now().atTime(10, 0);
        ticket = TicketEntity.builder()
                .id(1L)
                .branchId(1L)
                .entryTime(now)
                .build();

        charge = TicketChargeEntity.builder()
                .id(1L)
                .ticketId(1L)
                .totalAmount(BigDecimal.valueOf(50.00))
                .build();
    }

    @Test
    void execute_shouldGenerateCashClosingReport_whenDataExists() {
        Page<Branch> branches = new PageImpl<>(List.of(branch));
        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branches);
        when(ticketRepository.findByBranchIdOrderByEntryTimeDesc(anyLong())).thenReturn(List.of(ticket));
        when(ticketChargeRepository.findAll()).thenReturn(List.of(charge));

        List<Map<String, Object>> result = generateCashClosingReportUseCase.execute();

        assertThat(result).hasSize(1);
        Map<String, Object> branchReport = result.get(0);
        assertThat(branchReport.get("branch_id")).isEqualTo(1L);
        assertThat(branchReport.get("branch_name")).isEqualTo("Test Branch");
        assertThat(branchReport.get("date")).isEqualTo(LocalDate.now());
        assertThat(branchReport.get("total_tickets")).isEqualTo(1);
        assertThat(branchReport.get("total_cash")).isEqualTo(BigDecimal.valueOf(50.00));
    }

    @Test
    void execute_shouldReturnZeroCash_whenNoChargesExist() {
        Page<Branch> branches = new PageImpl<>(List.of(branch));
        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branches);
        when(ticketRepository.findByBranchIdOrderByEntryTimeDesc(anyLong())).thenReturn(List.of(ticket));
        when(ticketChargeRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = generateCashClosingReportUseCase.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("total_cash")).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void execute_shouldReturnEmptyReport_whenNoBranchesExist() {
        Page<Branch> branches = new PageImpl<>(List.of());
        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branches);

        List<Map<String, Object>> result = generateCashClosingReportUseCase.execute();

        assertThat(result).isEmpty();
    }
}
