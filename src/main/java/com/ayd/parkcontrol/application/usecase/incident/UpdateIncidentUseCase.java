package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.request.incident.UpdateIncidentRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentResponse;
import com.ayd.parkcontrol.application.mapper.IncidentMapper;
import com.ayd.parkcontrol.domain.exception.ResourceNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateIncidentUseCase {

    private final JpaIncidentRepository incidentRepository;
    private final JpaIncidentTypeRepository incidentTypeRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaUserRepository userRepository;
    private final JpaIncidentEvidenceRepository evidenceRepository;
    private final IncidentMapper mapper;

    @Transactional
    public IncidentResponse execute(Long incidentId, UpdateIncidentRequest request) {
        log.info("Updating incident with ID: {}", incidentId);

        IncidentEntity incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));

        // Update fields if provided
        if (request.getDescription() != null) {
            incident.setDescription(request.getDescription());
        }

        if (request.getResolutionNotes() != null) {
            incident.setResolutionNotes(request.getResolutionNotes());
        }

        IncidentEntity updatedIncident = incidentRepository.save(incident);

        // Load related entities for response
        IncidentTypeEntity incidentType = incidentTypeRepository.findById(updatedIncident.getIncidentTypeId())
                .orElse(null);

        BranchEntity branch = branchRepository.findById(updatedIncident.getBranchId())
                .orElse(null);

        UserEntity reportedByUser = userRepository.findById(updatedIncident.getReportedByUserId())
                .orElse(null);

        UserEntity resolvedByUser = updatedIncident.getResolvedByUserId() != null
                ? userRepository.findById(updatedIncident.getResolvedByUserId()).orElse(null)
                : null;

        Long evidenceCount = evidenceRepository.countByIncidentId(updatedIncident.getId());

        log.info("Incident updated successfully with ID: {}", incidentId);

        return mapper.toResponseWithDetails(
                updatedIncident, incidentType, branch, reportedByUser, resolvedByUser, evidenceCount);
    }
}
