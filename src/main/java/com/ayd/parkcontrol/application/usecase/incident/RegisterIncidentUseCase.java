package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.request.incident.RegisterIncidentRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentResponse;
import com.ayd.parkcontrol.application.mapper.IncidentMapper;
import com.ayd.parkcontrol.domain.exception.ResourceNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterIncidentUseCase {

    private final JpaIncidentRepository incidentRepository;
    private final JpaTicketRepository ticketRepository;
    private final JpaIncidentTypeRepository incidentTypeRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaUserRepository userRepository;
    private final JpaIncidentEvidenceRepository evidenceRepository;
    private final IncidentMapper mapper;

    @Transactional
    public IncidentResponse execute(RegisterIncidentRequest request) {
        log.info("Registering new incident for ticket: {}", request.getTicketId());

        // Validate ticket exists
        TicketEntity ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + request.getTicketId()));

        // Validate incident type exists
        IncidentTypeEntity incidentType = incidentTypeRepository.findById(request.getIncidentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Incident type not found with ID: " + request.getIncidentTypeId()));

        // Validate branch exists
        BranchEntity branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + request.getBranchId()));

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity reportedByUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create incident
        IncidentEntity incident = IncidentEntity.builder()
                .ticketId(request.getTicketId())
                .incidentTypeId(request.getIncidentTypeId())
                .branchId(request.getBranchId())
                .reportedByUserId(reportedByUser.getId())
                .licensePlate(request.getLicensePlate())
                .description(request.getDescription())
                .isResolved(false)
                .build();

        IncidentEntity savedIncident = incidentRepository.save(incident);

        // Update ticket to mark it has an incident
        ticket.setHasIncident(true);
        ticketRepository.save(ticket);

        log.info("Incident registered successfully with ID: {}", savedIncident.getId());

        Long evidenceCount = 0L;
        return mapper.toResponseWithDetails(
                savedIncident, incidentType, branch, reportedByUser, null, evidenceCount);
    }
}
