package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.incident.*;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

@Component
public class IncidentMapper {

    public IncidentResponse toResponse(IncidentEntity entity) {
        if (entity == null) {
            return null;
        }

        return IncidentResponse.builder()
                .id(entity.getId())
                .ticketId(entity.getTicketId())
                .incidentTypeId(entity.getIncidentTypeId())
                .branchId(entity.getBranchId())
                .reportedByUserId(entity.getReportedByUserId())
                .licensePlate(entity.getLicensePlate())
                .description(entity.getDescription())
                .resolutionNotes(entity.getResolutionNotes())
                .isResolved(entity.getIsResolved())
                .resolvedByUserId(entity.getResolvedByUserId())
                .resolvedAt(entity.getResolvedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public IncidentResponse toResponseWithDetails(IncidentEntity entity,
            IncidentTypeEntity incidentType,
            BranchEntity branch,
            UserEntity reportedByUser,
            UserEntity resolvedByUser,
            Long evidenceCount) {
        IncidentResponse response = toResponse(entity);

        if (response != null) {
            if (incidentType != null) {
                response.setIncidentTypeName(incidentType.getName());
            }
            if (branch != null) {
                response.setBranchName(branch.getName());
            }
            if (reportedByUser != null) {
                response.setReportedByUserName(
                        reportedByUser.getFirstName() + " " + reportedByUser.getLastName());
            }
            if (resolvedByUser != null) {
                response.setResolvedByUserName(
                        resolvedByUser.getFirstName() + " " + resolvedByUser.getLastName());
            }
            response.setEvidenceCount(evidenceCount != null ? evidenceCount : 0L);
        }

        return response;
    }

    public IncidentEvidenceResponse toEvidenceResponse(IncidentEvidenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return IncidentEvidenceResponse.builder()
                .id(entity.getId())
                .incidentId(entity.getIncidentId())
                .documentTypeId(entity.getDocumentTypeId())
                .filePath(entity.getFilePath())
                .fileName(entity.getFileName())
                .mimeType(entity.getMimeType())
                .fileSize(entity.getFileSize())
                .notes(entity.getNotes())
                .uploadedByUserId(entity.getUploadedByUserId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public IncidentEvidenceResponse toEvidenceResponseWithDetails(IncidentEvidenceEntity entity,
            DocumentTypeEntity documentType,
            UserEntity uploadedByUser) {
        IncidentEvidenceResponse response = toEvidenceResponse(entity);

        if (response != null) {
            if (documentType != null) {
                response.setDocumentTypeName(documentType.getName());
            }
            if (uploadedByUser != null) {
                response.setUploadedByUserName(
                        uploadedByUser.getFirstName() + " " + uploadedByUser.getLastName());
            }
        }

        return response;
    }

    public IncidentTypeResponse toIncidentTypeResponse(IncidentTypeEntity entity) {
        if (entity == null) {
            return null;
        }

        return IncidentTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .requiresDocumentation(entity.getRequiresDocumentation())
                .build();
    }

    public DocumentTypeResponse toDocumentTypeResponse(DocumentTypeEntity entity) {
        if (entity == null) {
            return null;
        }

        return DocumentTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
