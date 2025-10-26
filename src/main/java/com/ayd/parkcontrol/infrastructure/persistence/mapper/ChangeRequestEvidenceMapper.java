package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.validation.ChangeRequestEvidence;
import com.ayd.parkcontrol.infrastructure.persistence.entity.ChangeRequestEvidenceEntity;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestEvidenceMapper {

    public ChangeRequestEvidenceEntity toEntity(ChangeRequestEvidence domain) {
        if (domain == null) {
            return null;
        }

        return ChangeRequestEvidenceEntity.builder()
                .id(domain.getId())
                .changeRequestId(domain.getChangeRequestId())
                .documentTypeId(domain.getDocumentTypeId())
                .fileName(domain.getFileName())
                .fileUrl(domain.getFileUrl())
                .fileSize(domain.getFileSize())
                .uploadedBy(domain.getUploadedBy())
                .uploadedAt(domain.getUploadedAt())
                .build();
    }

    public ChangeRequestEvidence toDomain(ChangeRequestEvidenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return ChangeRequestEvidence.builder()
                .id(entity.getId())
                .changeRequestId(entity.getChangeRequestId())
                .documentTypeId(entity.getDocumentTypeId())
                .fileName(entity.getFileName())
                .fileUrl(entity.getFileUrl())
                .fileSize(entity.getFileSize())
                .uploadedBy(entity.getUploadedBy())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }
}
