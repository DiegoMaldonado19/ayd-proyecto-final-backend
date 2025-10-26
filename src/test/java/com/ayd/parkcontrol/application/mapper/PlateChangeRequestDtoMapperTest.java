package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.request.validation.CreatePlateChangeRequest;
import com.ayd.parkcontrol.application.dto.response.validation.EvidenceResponse;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.domain.model.validation.ChangeRequestEvidence;
import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PlateChangeRequestDtoMapperTest {

    private PlateChangeRequestDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PlateChangeRequestDtoMapper();
    }

    @Test
    void toDomain_shouldMapCorrectly() {
        CreatePlateChangeRequest request = CreatePlateChangeRequest.builder()
                .subscription_id(1L)
                .user_id(2L)
                .old_license_plate("P-123456")
                .new_license_plate("P-654321")
                .reason_id(1)
                .notes("Test notes")
                .build();

        PlateChangeRequest result = mapper.toDomain(request);

        assertThat(result).isNotNull();
        assertThat(result.getSubscriptionId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getOldLicensePlate()).isEqualTo("P-123456");
        assertThat(result.getNewLicensePlate()).isEqualTo("P-654321");
        assertThat(result.getReasonId()).isEqualTo(1);
        assertThat(result.getNotes()).isEqualTo("Test notes");
        assertThat(result.getStatusId()).isEqualTo(1);
    }

    @Test
    void toResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        PlateChangeRequest request = PlateChangeRequest.builder()
                .id(1L)
                .subscriptionId(1L)
                .userId(2L)
                .oldLicensePlate("P-123456")
                .newLicensePlate("P-654321")
                .reasonId(1)
                .notes("Test notes")
                .statusId(1)
                .reviewedBy(3L)
                .reviewedAt(now)
                .reviewNotes("Approved")
                .createdAt(now)
                .updatedAt(now)
                .build();

        PlateChangeRequestResponse result = mapper.toResponse(
                request,
                "Juan Pérez",
                "Robo",
                "PENDING",
                "Pendiente",
                "Carlos Rodríguez",
                2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUser_name()).isEqualTo("Juan Pérez");
        assertThat(result.getReason_name()).isEqualTo("Robo");
        assertThat(result.getStatus_code()).isEqualTo("PENDING");
        assertThat(result.getStatus_name()).isEqualTo("Pendiente");
        assertThat(result.getReviewer_name()).isEqualTo("Carlos Rodríguez");
        assertThat(result.getEvidence_count()).isEqualTo(2L);
    }

    @Test
    void toEvidenceResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        ChangeRequestEvidence evidence = ChangeRequestEvidence.builder()
                .id(1L)
                .changeRequestId(1L)
                .documentTypeId(1)
                .fileName("dpi.jpg")
                .fileUrl("https://storage.blob/dpi.jpg")
                .fileSize(1024L)
                .uploadedBy("test@email.com")
                .uploadedAt(now)
                .build();

        EvidenceResponse result = mapper.toEvidenceResponse(
                evidence,
                "IDENTIFICATION",
                "Identificación Personal");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getChange_request_id()).isEqualTo(1L);
        assertThat(result.getDocument_type_code()).isEqualTo("IDENTIFICATION");
        assertThat(result.getDocument_type_name()).isEqualTo("Identificación Personal");
        assertThat(result.getFile_name()).isEqualTo("dpi.jpg");
        assertThat(result.getFile_url()).isEqualTo("https://storage.blob/dpi.jpg");
        assertThat(result.getFile_size()).isEqualTo(1024L);
        assertThat(result.getUploaded_by()).isEqualTo("test@email.com");
    }
}
