package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketChargeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateCashClosingReportUseCase {

    private final BranchRepository branchRepository;
    private final JpaTicketRepository ticketRepository;
    private final JpaTicketChargeRepository ticketChargeRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> execute() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        var branches = branchRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        List<Map<String, Object>> cashClosingReport = new ArrayList<>();

        for (var branch : branches) {
            var todayTickets = ticketRepository.findByBranchIdOrderByEntryTimeDesc(branch.getId()).stream()
                    .filter(ticket -> ticket.getEntryTime() != null
                            && !ticket.getEntryTime().isBefore(startOfDay)
                            && ticket.getEntryTime().isBefore(endOfDay))
                    .toList();

            var ticketIds = todayTickets.stream().map(t -> t.getId()).toList();
            var todayCharges = ticketChargeRepository.findAll().stream()
                    .filter(charge -> ticketIds.contains(charge.getTicketId()))
                    .toList();

            BigDecimal totalCash = todayCharges.stream()
                    .map(charge -> charge.getTotalAmount() != null ? charge.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> branchClosing = new HashMap<>();
            branchClosing.put("branch_id", branch.getId());
            branchClosing.put("branch_name", branch.getName());
            branchClosing.put("date", LocalDate.now());
            branchClosing.put("total_tickets", todayTickets.size());
            branchClosing.put("total_cash", totalCash);

            cashClosingReport.add(branchClosing);
        }

        return cashClosingReport;
    }
}
