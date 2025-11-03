package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.validation.ChangeRequestEvidence;
import com.ayd.parkcontrol.infrastructure.persistence.entity.ChangeRequestEvidenceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ChangeRequestEvidenceMapperTest {

    private ChangeRequestEvidenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ChangeRequestEvidenceMapper();
    }

    @Test
    void toEntity_shouldReturnNull_whenDomainIsNull() {
        ChangeRequestEvidenceEntity result = mapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldMapCorrectly_whenDomainIsValid() {
        LocalDateTime now = LocalDateTime.now();
        ChangeRequestEvidence domain = ChangeRequestEvidence.builder()
                .id(1L)
                .changeRequestId(1L)
                .documentTypeId(1)
                .fileName("dpi_frontal.jpg")
                .fileUrl("https://storage.blob.core.windows.net/documentos-cambios/dpi_frontal.jpg")
                .fileSize(2048576L)
                .uploadedBy("user@parkcontrol.com")
                .uploadedAt(now)
                .build();

        ChangeRequestEvidenceEntity result = mapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getChangeRequestId()).isEqualTo(1L);
        assertThat(result.getDocumentTypeId()).isEqualTo(1);
        assertThat(result.getFileName()).isEqualTo("dpi_frontal.jpg");
        assertThat(result.getFileUrl())
                .isEqualTo("https://storage.blob.core.windows.net/documentos-cambios/dpi_frontal.jpg");
        assertThat(result.getFileSize()).isEqualTo(2048576L);
        assertThat(result.getUploadedBy()).isEqualTo("user@parkcontrol.com");
        assertThat(result.getUploadedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        ChangeRequestEvidence result = mapper.toDomain(null);

        assertThat(result).isNull();
    }

    @Test
    void toDomain_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        ChangeRequestEvidenceEntity entity = ChangeRequestEvidenceEntity.builder()
                .id(2L)
                .changeRequestId(2L)
                .documentTypeId(2)
                .fileName("tarjeta_circulacion.pdf")
                .fileUrl("https://storage.blob.core.windows.net/documentos-cambios/tarjeta_circulacion.pdf")
                .fileSize(1024000L)
                .uploadedBy("admin@parkcontrol.com")
                .uploadedAt(now)
                .build();

        ChangeRequestEvidence result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getChangeRequestId()).isEqualTo(2L);
        assertThat(result.getDocumentTypeId()).isEqualTo(2);
        assertThat(result.getFileName()).isEqualTo("tarjeta_circulacion.pdf");
        assertThat(result.getFileUrl())
                .isEqualTo("https://storage.blob.core.windows.net/documentos-cambios/tarjeta_circulacion.pdf");
        assertThat(result.getFileSize()).isEqualTo(1024000L);
        assertThat(result.getUploadedBy()).isEqualTo("admin@parkcontrol.com");
        assertThat(result.getUploadedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldHandleNullFileSize() {
        LocalDateTime now = LocalDateTime.now();
        ChangeRequestEvidence domain = ChangeRequestEvidence.builder()
                .id(3L)
                .changeRequestId(3L)
                .documentTypeId(3)
                .fileName("denuncia_policial.pdf")
                .fileUrl("https://storage.blob.core.windows.net/documentos-cambios/denuncia_policial.pdf")
                .fileSize(null)
                .uploadedBy("operator@parkcontrol.com")
                .uploadedAt(now)
                .build();

        ChangeRequestEvidenceEntity result = mapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getFileSize()).isNull();
    }

    @Test
    void toDomain_shouldHandleNullFileSize() {
        LocalDateTime now = LocalDateTime.now();
        ChangeRequestEvidenceEntity entity = ChangeRequestEvidenceEntity.builder()
                .id(4L)
                .changeRequestId(4L)
                .documentTypeId(4)
                .fileName("documento_traspaso.pdf")
                .fileUrl("https://storage.blob.core.windows.net/documentos-cambios/documento_traspaso.pdf")
                .fileSize(null)
                .uploadedBy("backoffice@parkcontrol.com")
                .uploadedAt(now)
                .build();

        ChangeRequestEvidence result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getFileSize()).isNull();
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        LocalDateTime now = LocalDateTime.now();
        ChangeRequestEvidence originalEvidence = ChangeRequestEvidence.builder()
                .id(5L)
                .changeRequestId(5L)
                .documentTypeId(5)
                .fileName("reporte_seguro.pdf")
                .fileUrl("https://storage.blob.core.windows.net/documentos-cambios/reporte_seguro.pdf")
                .fileSize(3145728L)
                .uploadedBy("client@parkcontrol.com")
                .uploadedAt(now)
                .build();

        ChangeRequestEvidenceEntity entity = mapper.toEntity(originalEvidence);
        ChangeRequestEvidence resultEvidence = mapper.toDomain(entity);

        assertThat(resultEvidence).isNotNull();
        assertThat(resultEvidence.getId()).isEqualTo(originalEvidence.getId());
        assertThat(resultEvidence.getChangeRequestId()).isEqualTo(originalEvidence.getChangeRequestId());
        assertThat(resultEvidence.getDocumentTypeId()).isEqualTo(originalEvidence.getDocumentTypeId());
        assertThat(resultEvidence.getFileName()).isEqualTo(originalEvidence.getFileName());
        assertThat(resultEvidence.getFileUrl()).isEqualTo(originalEvidence.getFileUrl());
        assertThat(resultEvidence.getFileSize()).isEqualTo(originalEvidence.getFileSize());
        assertThat(resultEvidence.getUploadedBy()).isEqualTo(originalEvidence.getUploadedBy());
        assertThat(resultEvidence.getUploadedAt()).isEqualTo(originalEvidence.getUploadedAt());
    }

    @Test
    void toEntity_shouldHandleDifferentDocumentTypes() {
        LocalDateTime now = LocalDateTime.now();

        ChangeRequestEvidence identification = ChangeRequestEvidence.builder()
                .id(1L)
                .changeRequestId(1L)
                .documentTypeId(1)
                .fileName("identification.jpg")
                .fileUrl("https://storage/identification.jpg")
                .fileSize(1024L)
                .uploadedBy("user1@parkcontrol.com")
                .uploadedAt(now)
                .build();

        ChangeRequestEvidence vehicleCard = ChangeRequestEvidence.builder()
                .id(2L)
                .changeRequestId(1L)
                .documentTypeId(2)
                .fileName("vehicle_card.pdf")
                .fileUrl("https://storage/vehicle_card.pdf")
                .fileSize(2048L)
                .uploadedBy("user1@parkcontrol.com")
                .uploadedAt(now)
                .build();

        ChangeRequestEvidence policeReport = ChangeRequestEvidence.builder()
                .id(3L)
                .changeRequestId(1L)
                .documentTypeId(3)
                .fileName("police_report.pdf")
                .fileUrl("https://storage/police_report.pdf")
                .fileSize(4096L)
                .uploadedBy("user1@parkcontrol.com")
                .uploadedAt(now)
                .build();

        ChangeRequestEvidenceEntity identificationEntity = mapper.toEntity(identification);
        ChangeRequestEvidenceEntity vehicleCardEntity = mapper.toEntity(vehicleCard);
        ChangeRequestEvidenceEntity policeReportEntity = mapper.toEntity(policeReport);

        assertThat(identificationEntity.getDocumentTypeId()).isEqualTo(1);
        assertThat(vehicleCardEntity.getDocumentTypeId()).isEqualTo(2);
        assertThat(policeReportEntity.getDocumentTypeId()).isEqualTo(3);
    }

    @Test
    void toDomain_shouldHandleMultipleEvidencesForSameRequest() {
        LocalDateTime now = LocalDateTime.now();

        ChangeRequestEvidenceEntity evidence1 = ChangeRequestEvidenceEntity.builder()
                .id(1L)
                .changeRequestId(1L)
                .documentTypeId(1)
                .fileName("evidence1.jpg")
                .fileUrl("https://storage/evidence1.jpg")
                .fileSize(500000L)
                .uploadedBy("user@parkcontrol.com")
                .uploadedAt(now)
                .build();

        ChangeRequestEvidenceEntity evidence2 = ChangeRequestEvidenceEntity.builder()
                .id(2L)
                .changeRequestId(1L)
                .documentTypeId(6)
                .fileName("evidence2.jpg")
                .fileUrl("https://storage/evidence2.jpg")
                .fileSize(600000L)
                .uploadedBy("user@parkcontrol.com")
                .uploadedAt(now.plusMinutes(5))
                .build();

        ChangeRequestEvidence result1 = mapper.toDomain(evidence1);
        ChangeRequestEvidence result2 = mapper.toDomain(evidence2);

        assertThat(result1.getChangeRequestId()).isEqualTo(1L);
        assertThat(result2.getChangeRequestId()).isEqualTo(1L);
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
        assertThat(result1.getFileName()).isNotEqualTo(result2.getFileName());
    }

    @Test
    void toEntity_shouldHandleLargeFiles() {
        LocalDateTime now = LocalDateTime.now();
        ChangeRequestEvidence domain = ChangeRequestEvidence.builder()
                .id(6L)
                .changeRequestId(6L)
                .documentTypeId(6)
                .fileName("large_photo.jpg")
                .fileUrl("https://storage.blob.core.windows.net/documentos-cambios/large_photo.jpg")
                .fileSize(10485760L)
                .uploadedBy("photographer@parkcontrol.com")
                .uploadedAt(now)
                .build();

        ChangeRequestEvidenceEntity result = mapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getFileSize()).isEqualTo(10485760L);
    }
}
