package com.ayd.parkcontrol.application.usecase.settlement;

import com.ayd.parkcontrol.application.dto.request.settlement.GenerateSettlementRequest;
import com.ayd.parkcontrol.application.dto.response.settlement.SettlementResponse;
import com.ayd.parkcontrol.application.dto.response.settlement.SettlementTicketDetail;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateSettlementUseCase {

    private final JpaBusinessSettlementHistoryRepository settlementRepository;
    private final JpaBusinessFreeHoursRepository businessFreeHoursRepository;
    private final JpaAffiliatedBusinessRepository commerceRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaUserRepository userRepository;
    private final JpaSettlementTicketRepository settlementTicketRepository;
    private final JpaTicketRepository ticketRepository;

    @Transactional
    public SettlementResponse execute(GenerateSettlementRequest request) {
        log.info("Generating settlement for business {} at branch {} for period {} to {}",
                request.getBusinessId(), request.getBranchId(), request.getPeriodStart(), request.getPeriodEnd());

        if (request.getPeriodEnd().isBefore(request.getPeriodStart())
                || request.getPeriodEnd().isEqual(request.getPeriodStart())) {
            throw new BusinessRuleException("Period end must be after period start");
        }

        AffiliatedBusinessEntity commerce = commerceRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new NotFoundException("Commerce not found with ID: " + request.getBusinessId()));

        BranchEntity branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new NotFoundException("Branch not found with ID: " + request.getBranchId()));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        List<BusinessFreeHoursEntity> unsettledHours = businessFreeHoursRepository
                .findUnsettledByBusinessBranchAndPeriod(
                        request.getBusinessId(),
                        request.getBranchId(),
                        request.getPeriodStart(),
                        request.getPeriodEnd());

        if (unsettledHours.isEmpty()) {
            throw new BusinessRuleException("No unsettled free hours found for the specified period");
        }

        BigDecimal totalHours = unsettledHours.stream()
                .map(BusinessFreeHoursEntity::getGrantedHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = totalHours.multiply(commerce.getRatePerHour());

        BusinessSettlementHistoryEntity settlement = BusinessSettlementHistoryEntity.builder()
                .businessId(request.getBusinessId())
                .branchId(request.getBranchId())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .totalHours(totalHours)
                .totalAmount(totalAmount)
                .ticketCount(unsettledHours.size())
                .settledAt(LocalDateTime.now())
                .settledBy(user.getId())
                .observations(request.getObservations())
                .build();

        BusinessSettlementHistoryEntity saved = settlementRepository.save(settlement);

        for (BusinessFreeHoursEntity freeHours : unsettledHours) {
            SettlementTicketEntity settlementTicket = SettlementTicketEntity.builder()
                    .settlementId(saved.getId())
                    .ticketId(freeHours.getTicketId())
                    .freeHoursGranted(freeHours.getGrantedHours())
                    .build();
            settlementTicketRepository.save(settlementTicket);
        }

        List<Long> freeHoursIds = unsettledHours.stream()
                .map(BusinessFreeHoursEntity::getId)
                .collect(Collectors.toList());
        businessFreeHoursRepository.markAsSettled(freeHoursIds);

        log.info("Settlement generated successfully with ID: {}, Total: {}", saved.getId(), totalAmount);

        return mapToResponse(saved, commerce, branch, user, unsettledHours);
    }

    private SettlementResponse mapToResponse(BusinessSettlementHistoryEntity entity,
            AffiliatedBusinessEntity commerce,
            BranchEntity branch, UserEntity user,
            List<BusinessFreeHoursEntity> freeHours) {
        List<SettlementTicketDetail> ticketDetails = freeHours.stream()
                .map(fh -> {
                    TicketEntity ticket = ticketRepository.findById(fh.getTicketId()).orElse(null);
                    return SettlementTicketDetail.builder()
                            .ticketId(fh.getTicketId())
                            .folio(ticket != null ? ticket.getFolio() : null)
                            .licensePlate(ticket != null ? ticket.getLicensePlate() : null)
                            .freeHoursGranted(fh.getGrantedHours())
                            .build();
                })
                .collect(Collectors.toList());

        return SettlementResponse.builder()
                .id(entity.getId())
                .businessId(entity.getBusinessId())
                .businessName(commerce.getName())
                .businessTaxId(commerce.getTaxId())
                .branchId(entity.getBranchId())
                .branchName(branch.getName())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .totalHours(entity.getTotalHours())
                .totalAmount(entity.getTotalAmount())
                .ticketCount(entity.getTicketCount())
                .settledAt(entity.getSettledAt())
                .settledBy(entity.getSettledBy())
                .settledByName(user.getFirstName() + " " + user.getLastName())
                .observations(entity.getObservations())
                .createdAt(entity.getCreatedAt())
                .tickets(ticketDetails)
                .build();
    }
}
