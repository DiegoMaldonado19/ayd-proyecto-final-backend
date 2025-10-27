package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.application.dto.response.report.BillingReportResponse;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketChargeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateBillingReportUseCase {

    private final BranchRepository branchRepository;
    private final JpaTicketRepository ticketRepository;
    private final JpaTicketChargeRepository ticketChargeRepository;

    @Transactional(readOnly = true)
    public List<BillingReportResponse> execute() {
        var branches = branchRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        List<BillingReportResponse> report = new ArrayList<>();

        for (var branch : branches) {
            var branchTickets = ticketRepository.findByBranchIdOrderByEntryTimeDesc(branch.getId());
            var ticketIds = branchTickets.stream().map(t -> t.getId()).toList();

            var charges = ticketChargeRepository.findAll().stream()
                    .filter(charge -> ticketIds.contains(charge.getTicketId()))
                    .toList();

            long totalTickets = charges.size();
            BigDecimal totalRevenue = charges.stream()
                    .map(charge -> charge.getTotalAmount() != null ? charge.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal averageTicketValue = BigDecimal.ZERO;
            if (totalTickets > 0) {
                averageTicketValue = totalRevenue.divide(
                        BigDecimal.valueOf(totalTickets),
                        2,
                        RoundingMode.HALF_UP);
            }

            report.add(BillingReportResponse.builder()
                    .branchId(branch.getId())
                    .branchName(branch.getName())
                    .totalTickets(totalTickets)
                    .totalRevenue(totalRevenue)
                    .averageTicketValue(averageTicketValue)
                    .paymentMethod("EFECTIVO") // Por defecto, la entidad no tiene este campo
                    .build());
        }

        return report;
    }
}
