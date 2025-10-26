package com.ayd.parkcontrol.application.usecase.settlement;

import com.ayd.parkcontrol.application.dto.response.settlement.SettlementResponse;
import com.ayd.parkcontrol.application.dto.response.settlement.SettlementTicketDetail;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetSettlementByIdUseCase {

    private final JpaBusinessSettlementHistoryRepository settlementRepository;
    private final JpaSettlementTicketRepository settlementTicketRepository;
    private final JpaAffiliatedBusinessRepository commerceRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaUserRepository userRepository;
    private final JpaTicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public SettlementResponse execute(Long id) {
        log.info("Fetching settlement with ID: {}", id);

        BusinessSettlementHistoryEntity settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Settlement not found with ID: " + id));

        return mapToResponse(settlement);
    }

    private SettlementResponse mapToResponse(BusinessSettlementHistoryEntity entity) {
        AffiliatedBusinessEntity commerce = commerceRepository.findById(entity.getBusinessId()).orElse(null);
        BranchEntity branch = branchRepository.findById(entity.getBranchId()).orElse(null);
        UserEntity user = userRepository.findById(entity.getSettledBy()).orElse(null);

        List<SettlementTicketEntity> settlementTickets = settlementTicketRepository.findBySettlementId(entity.getId());

        List<SettlementTicketDetail> ticketDetails = settlementTickets.stream()
                .map(st -> {
                    TicketEntity ticket = ticketRepository.findById(st.getTicketId()).orElse(null);
                    return SettlementTicketDetail.builder()
                            .ticketId(st.getTicketId())
                            .folio(ticket != null ? ticket.getFolio() : null)
                            .licensePlate(ticket != null ? ticket.getLicensePlate() : null)
                            .freeHoursGranted(st.getFreeHoursGranted())
                            .build();
                })
                .collect(Collectors.toList());

        return SettlementResponse.builder()
                .id(entity.getId())
                .businessId(entity.getBusinessId())
                .businessName(commerce != null ? commerce.getName() : null)
                .businessTaxId(commerce != null ? commerce.getTaxId() : null)
                .branchId(entity.getBranchId())
                .branchName(branch != null ? branch.getName() : null)
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .totalHours(entity.getTotalHours())
                .totalAmount(entity.getTotalAmount())
                .ticketCount(entity.getTicketCount())
                .settledAt(entity.getSettledAt())
                .settledBy(entity.getSettledBy())
                .settledByName(user != null ? user.getFirstName() + " " + user.getLastName() : null)
                .observations(entity.getObservations())
                .createdAt(entity.getCreatedAt())
                .tickets(ticketDetails)
                .build();
    }
}
