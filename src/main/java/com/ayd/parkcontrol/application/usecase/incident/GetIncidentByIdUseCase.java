package com.ayd.parkcontrol.application.usecase.incident;

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
public class GetIncidentByIdUseCase {

    private final JpaIncidentRepository incidentRepository;
    private final JpaIncidentTypeRepository incidentTypeRepository;
    private final JpaBranchRepository branchRepository;
    private final JpaUserRepository userRepository;
    private final JpaIncidentEvidenceRepository evidenceRepository;
    private final IncidentMapper mapper;

    @Transactional(readOnly = true)
    public IncidentResponse execute(Long incidentId) {
        log.info("Getting incident by ID: {}", incidentId);

        IncidentEntity incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));

        IncidentTypeEntity incidentType = incidentTypeRepository.findById(incident.getIncidentTypeId())
                .orElse(null);

        BranchEntity branch = branchRepository.findById(incident.getBranchId())
                .orElse(null);

        UserEntity reportedByUser = userRepository.findById(incident.getReportedByUserId())
                .orElse(null);

        UserEntity resolvedByUser = incident.getResolvedByUserId() != null
                ? userRepository.findById(incident.getResolvedByUserId()).orElse(null)
                : null;

        Long evidenceCount = evidenceRepository.countByIncidentId(incident.getId());

        return mapper.toResponseWithDetails(
                incident, incidentType, branch, reportedByUser, resolvedByUser, evidenceCount);
    }
}
