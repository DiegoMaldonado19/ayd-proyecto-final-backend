package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.response.incident.IncidentEvidenceResponse;
import com.ayd.parkcontrol.application.mapper.IncidentMapper;
import com.ayd.parkcontrol.domain.exception.ResourceNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListEvidencesByIncidentUseCase {

    private final JpaIncidentRepository incidentRepository;
    private final JpaIncidentEvidenceRepository evidenceRepository;
    private final JpaDocumentTypeRepository documentTypeRepository;
    private final JpaUserRepository userRepository;
    private final IncidentMapper mapper;

    @Transactional(readOnly = true)
    public List<IncidentEvidenceResponse> execute(Long incidentId) {
        log.info("Listing evidences for incident ID: {}", incidentId);

        // Validate incident exists
        incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));

        List<IncidentEvidenceEntity> evidences = evidenceRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId);

        return evidences.stream()
                .map(this::mapToResponseWithDetails)
                .collect(Collectors.toList());
    }

    private IncidentEvidenceResponse mapToResponseWithDetails(IncidentEvidenceEntity evidence) {
        DocumentTypeEntity documentType = documentTypeRepository.findById(evidence.getDocumentTypeId())
                .orElse(null);

        UserEntity uploadedByUser = userRepository.findById(evidence.getUploadedByUserId())
                .orElse(null);

        return mapper.toEvidenceResponseWithDetails(evidence, documentType, uploadedByUser);
    }
}
