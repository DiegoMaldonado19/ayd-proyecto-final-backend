package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.request.ticket.ApplyBenefitRequest;
import com.ayd.parkcontrol.application.dto.response.ticket.BusinessFreeHoursResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BusinessFreeHoursEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBusinessFreeHoursRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyCommerceBenefitUseCase {

    private final JpaTicketRepository ticketRepository;
    private final JpaBusinessFreeHoursRepository businessFreeHoursRepository;
    private final JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Transactional
    public BusinessFreeHoursResponse execute(Long ticketId, ApplyBenefitRequest request) {
        log.info("Applying benefit from business {} to ticket {}", request.getBusinessId(), ticketId);

        // 1. Validar ticket
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found with ID: " + ticketId));

        // 2. Validar que el ticket esté en progreso
        TicketStatusTypeEntity inProgressStatus = ticketStatusTypeRepository.findByCode("IN_PROGRESS")
                .orElseThrow(() -> new NotFoundException("Ticket status 'IN_PROGRESS' not found"));

        if (!ticket.getStatusTypeId().equals(inProgressStatus.getId())) {
            throw new BusinessRuleException("Cannot apply benefit to a ticket that is not in progress");
        }

        // 3. Validar horas otorgadas
        if (request.getGrantedHours() == null || request.getGrantedHours() <= 0) {
            throw new BusinessRuleException("Granted hours must be greater than zero");
        }

        // 4. Crear registro de horas gratis
        BusinessFreeHoursEntity freeHours = BusinessFreeHoursEntity.builder()
                .ticketId(ticketId)
                .businessId(request.getBusinessId())
                .branchId(ticket.getBranchId())
                .grantedHours(BigDecimal.valueOf(request.getGrantedHours()))
                .grantedAt(LocalDateTime.now())
                .isSettled(false)
                .build();

        BusinessFreeHoursEntity saved = businessFreeHoursRepository.save(freeHours);

        log.info("Benefit applied successfully. Free hours ID: {}, Hours: {}",
                saved.getId(), saved.getGrantedHours());

        return BusinessFreeHoursResponse.builder()
                .id(saved.getId())
                .ticketId(saved.getTicketId())
                .businessId(saved.getBusinessId())
                .businessName("Business #" + saved.getBusinessId()) // TODO: Obtener nombre real
                .branchId(saved.getBranchId())
                .grantedHours(saved.getGrantedHours())
                .grantedAt(saved.getGrantedAt())
                .isSettled(saved.getIsSettled())
                .build();
    }
}
