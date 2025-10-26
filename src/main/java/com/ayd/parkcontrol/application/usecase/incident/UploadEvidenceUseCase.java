package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.request.incident.UploadEvidenceRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentEvidenceResponse;
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
public class UploadEvidenceUseCase {

    private final JpaIncidentRepository incidentRepository;
    private final JpaIncidentEvidenceRepository evidenceRepository;
    private final JpaDocumentTypeRepository documentTypeRepository;
    private final JpaUserRepository userRepository;
    private final IncidentMapper mapper;

    @Transactional
    public IncidentEvidenceResponse execute(Long incidentId, UploadEvidenceRequest request) {
        log.info("Uploading evidence for incident ID: {}", incidentId);

        // Validate incident exists
        incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));

        // Validate document type exists
        DocumentTypeEntity documentType = documentTypeRepository.findById(request.getDocumentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document type not found with ID: " + request.getDocumentTypeId()));

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity uploadedByUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create evidence record
        IncidentEvidenceEntity evidence = IncidentEvidenceEntity.builder()
                .incidentId(incidentId)
                .documentTypeId(request.getDocumentTypeId())
                .filePath(request.getFilePath())
                .fileName(request.getFileName())
                .mimeType(request.getMimeType())
                .fileSize(request.getFileSize())
                .notes(request.getNotes())
                .uploadedByUserId(uploadedByUser.getId())
                .build();

        IncidentEvidenceEntity savedEvidence = evidenceRepository.save(evidence);

        log.info("Evidence uploaded successfully with ID: {}", savedEvidence.getId());

        return mapper.toEvidenceResponseWithDetails(savedEvidence, documentType, uploadedByUser);
    }
}
