package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.incident.*;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentMapperTest {

    private IncidentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IncidentMapper();
    }

    @Test
    void toResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        IncidentEntity entity = new IncidentEntity();
        entity.setId(1L);
        entity.setTicketId(100L);
        entity.setIncidentTypeId(1);
        entity.setBranchId(5L);
        entity.setReportedByUserId(200L);
        entity.setLicensePlate("P-123ABC");
        entity.setDescription("Incidente reportado");
        entity.setResolutionNotes("Resuelto exitosamente");
        entity.setIsResolved(true);
        entity.setResolvedByUserId(201L);
        entity.setResolvedAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        IncidentResponse result = mapper.toResponse(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTicketId()).isEqualTo(100L);
        assertThat(result.getIncidentTypeId()).isEqualTo(1);
        assertThat(result.getBranchId()).isEqualTo(5L);
        assertThat(result.getReportedByUserId()).isEqualTo(200L);
        assertThat(result.getLicensePlate()).isEqualTo("P-123ABC");
        assertThat(result.getDescription()).isEqualTo("Incidente reportado");
        assertThat(result.getResolutionNotes()).isEqualTo("Resuelto exitosamente");
        assertThat(result.getIsResolved()).isTrue();
        assertThat(result.getResolvedByUserId()).isEqualTo(201L);
        assertThat(result.getResolvedAt()).isEqualTo(now);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_withNullInput_shouldReturnNull() {
        IncidentResponse result = mapper.toResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toResponseWithDetails_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        IncidentEntity entity = new IncidentEntity();
        entity.setId(1L);
        entity.setTicketId(100L);
        entity.setIncidentTypeId(1);
        entity.setBranchId(5L);
        entity.setReportedByUserId(200L);
        entity.setCreatedAt(now);

        IncidentTypeEntity incidentType = new IncidentTypeEntity();
        incidentType.setName("Accidente de tránsito");

        BranchEntity branch = new BranchEntity();
        branch.setName("Sucursal Central");

        UserEntity reportedByUser = new UserEntity();
        reportedByUser.setFirstName("Juan");
        reportedByUser.setLastName("Pérez");

        UserEntity resolvedByUser = new UserEntity();
        resolvedByUser.setFirstName("Carlos");
        resolvedByUser.setLastName("Rodríguez");

        IncidentResponse result = mapper.toResponseWithDetails(entity, incidentType, branch,
                reportedByUser, resolvedByUser, 3L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getIncidentTypeName()).isEqualTo("Accidente de tránsito");
        assertThat(result.getBranchName()).isEqualTo("Sucursal Central");
        assertThat(result.getReportedByUserName()).isEqualTo("Juan Pérez");
        assertThat(result.getResolvedByUserName()).isEqualTo("Carlos Rodríguez");
        assertThat(result.getEvidenceCount()).isEqualTo(3L);
    }

    @Test
    void toEvidenceResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        IncidentEvidenceEntity entity = new IncidentEvidenceEntity();
        entity.setId(1L);
        entity.setIncidentId(10L);
        entity.setDocumentTypeId(1);
        entity.setFilePath("/uploads/evidence.jpg");
        entity.setFileName("evidence.jpg");
        entity.setMimeType("image/jpeg");
        entity.setFileSize(2048L);
        entity.setNotes("Foto del vehículo dañado");
        entity.setUploadedByUserId(200L);
        entity.setCreatedAt(now);

        IncidentEvidenceResponse result = mapper.toEvidenceResponse(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getIncidentId()).isEqualTo(10L);
        assertThat(result.getDocumentTypeId()).isEqualTo(1);
        assertThat(result.getFilePath()).isEqualTo("/uploads/evidence.jpg");
        assertThat(result.getFileName()).isEqualTo("evidence.jpg");
        assertThat(result.getMimeType()).isEqualTo("image/jpeg");
        assertThat(result.getFileSize()).isEqualTo(2048L);
        assertThat(result.getNotes()).isEqualTo("Foto del vehículo dañado");
        assertThat(result.getUploadedByUserId()).isEqualTo(200L);
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toEvidenceResponse_withNullInput_shouldReturnNull() {
        IncidentEvidenceResponse result = mapper.toEvidenceResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toEvidenceResponseWithDetails_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        IncidentEvidenceEntity entity = new IncidentEvidenceEntity();
        entity.setId(1L);
        entity.setIncidentId(10L);
        entity.setCreatedAt(now);

        DocumentTypeEntity documentType = new DocumentTypeEntity();
        documentType.setName("Fotografía");

        UserEntity uploadedByUser = new UserEntity();
        uploadedByUser.setFirstName("María");
        uploadedByUser.setLastName("González");

        IncidentEvidenceResponse result = mapper.toEvidenceResponseWithDetails(entity,
                documentType, uploadedByUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDocumentTypeName()).isEqualTo("Fotografía");
        assertThat(result.getUploadedByUserName()).isEqualTo("María González");
    }

    @Test
    void toIncidentTypeResponse_shouldMapCorrectly() {
        IncidentTypeEntity entity = new IncidentTypeEntity();
        entity.setId(1);
        entity.setCode("ACCIDENT");
        entity.setName("Accidente");
        entity.setRequiresDocumentation(true);

        IncidentTypeResponse result = mapper.toIncidentTypeResponse(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCode()).isEqualTo("ACCIDENT");
        assertThat(result.getName()).isEqualTo("Accidente");
        assertThat(result.getRequiresDocumentation()).isTrue();
    }

    @Test
    void toIncidentTypeResponse_withNullInput_shouldReturnNull() {
        IncidentTypeResponse result = mapper.toIncidentTypeResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toDocumentTypeResponse_shouldMapCorrectly() {
        DocumentTypeEntity entity = new DocumentTypeEntity();
        entity.setId(1);
        entity.setCode("PHOTO");
        entity.setName("Fotografía");
        entity.setDescription("Evidencia fotográfica");

        DocumentTypeResponse result = mapper.toDocumentTypeResponse(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCode()).isEqualTo("PHOTO");
        assertThat(result.getName()).isEqualTo("Fotografía");
        assertThat(result.getDescription()).isEqualTo("Evidencia fotográfica");
    }

    @Test
    void toDocumentTypeResponse_withNullInput_shouldReturnNull() {
        DocumentTypeResponse result = mapper.toDocumentTypeResponse(null);
        assertThat(result).isNull();
    }
}
