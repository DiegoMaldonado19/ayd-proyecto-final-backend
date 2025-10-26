package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.request.incident.ResolveIncidentRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentResponse;
import com.ayd.parkcontrol.application.mapper.IncidentMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.ResourceNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResolveIncidentUseCase {

    private final JpaIncidentRepository incidentRepository;
    private final JpaIncidentTypeRepository incidentTypeRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaUserRepository userRepository;
    private final JpaIncidentEvidenceRepository evidenceRepository;
    private final IncidentMapper mapper;

    @Transactional
    public IncidentResponse execute(Long incidentId, ResolveIncidentRequest request) {
        log.info("Resolving incident with ID: {}", incidentId);

        IncidentEntity incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));

        if (incident.getIsResolved()) {
            throw new BusinessRuleException("Incident is already resolved");
        }

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity resolvedByUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Resolve incident
        incident.setIsResolved(true);
        incident.setResolvedByUserId(resolvedByUser.getId());
        incident.setResolvedAt(LocalDateTime.now());
        incident.setResolutionNotes(request.getResolutionNotes());

        IncidentEntity resolvedIncident = incidentRepository.save(incident);

        // Load related entities for response
        IncidentTypeEntity incidentType = incidentTypeRepository.findById(resolvedIncident.getIncidentTypeId())
                .orElse(null);

        BranchEntity branch = branchRepository.findById(resolvedIncident.getBranchId())
                .orElse(null);

        UserEntity reportedByUser = userRepository.findById(resolvedIncident.getReportedByUserId())
                .orElse(null);

        Long evidenceCount = evidenceRepository.countByIncidentId(resolvedIncident.getId());

        log.info("Incident resolved successfully with ID: {}", incidentId);

        return mapper.toResponseWithDetails(
                resolvedIncident, incidentType, branch, reportedByUser, resolvedByUser, evidenceCount);
    }
}
