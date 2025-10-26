package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.request.incident.UploadEvidenceRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentEvidenceResponse;
import com.ayd.parkcontrol.application.mapper.IncidentMapper;
import com.ayd.parkcontrol.application.port.output.BlobStorageService;
import com.ayd.parkcontrol.domain.exception.ResourceNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadEvidenceUseCase {

    private final JpaIncidentRepository incidentRepository;
    private final JpaIncidentEvidenceRepository evidenceRepository;
    private final JpaDocumentTypeRepository documentTypeRepository;
    private final JpaUserRepository userRepository;
    private final IncidentMapper mapper;
    private final BlobStorageService blobStorageService;

    @Value("${azure.storage.container.incidents:evidencias-incidentes}")
    private String incidentsContainer;

    @Transactional
    public IncidentEvidenceResponse execute(Long incidentId, UploadEvidenceRequest request) {
        log.info("Uploading evidence for incident ID: {}", incidentId);

        MultipartFile file = request.getFile();

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file size (10MB max)
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

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

        // Generate unique blob name
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String sanitizedFilename = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
        String blobName = String.format("incident_%d/%s_%s", incidentId, timestamp, sanitizedFilename);

        // Upload to Azure Blob Storage
        String fileUrl;
        try {
            fileUrl = blobStorageService.uploadFile(incidentsContainer, file, blobName);
            log.info("File uploaded to Azure Storage: {}", fileUrl);
        } catch (Exception e) {
            log.error("Failed to upload file to Azure Storage: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to Azure Storage. Please try again later.", e);
        }

        // Create evidence record
        IncidentEvidenceEntity evidence = IncidentEvidenceEntity.builder()
                .incidentId(incidentId)
                .documentTypeId(request.getDocumentTypeId())
                .filePath(fileUrl)
                .fileName(sanitizedFilename)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .notes(request.getNotes())
                .uploadedByUserId(uploadedByUser.getId())
                .build();

        IncidentEvidenceEntity savedEvidence = evidenceRepository.save(evidence);

        log.info("Evidence uploaded successfully with ID: {}", savedEvidence.getId());

        return mapper.toEvidenceResponseWithDetails(savedEvidence, documentType, uploadedByUser);
    }
}
