package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PlateChangeRequestEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PlateChangeRequestMapperTest {

    private PlateChangeRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PlateChangeRequestMapper();
    }

    @Test
    void toEntity_shouldReturnNull_whenDomainIsNull() {
        PlateChangeRequestEntity result = mapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldMapCorrectly_whenDomainIsValid() {
        LocalDateTime now = LocalDateTime.now();
        PlateChangeRequest domain = PlateChangeRequest.builder()
                .id(1L)
                .subscriptionId(1L)
                .userId(2L)
                .oldLicensePlate("ABC-123")
                .newLicensePlate("DEF-456")
                .reasonId(1)
                .notes("Vehicle sold")
                .statusId(1)
                .reviewedBy(3L)
                .reviewedAt(now)
                .reviewNotes("Approved after verification")
                .createdAt(now)
                .updatedAt(now)
                .build();

        PlateChangeRequestEntity result = mapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSubscriptionId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getOldLicensePlate()).isEqualTo("ABC-123");
        assertThat(result.getNewLicensePlate()).isEqualTo("DEF-456");
        assertThat(result.getReasonId()).isEqualTo(1);
        assertThat(result.getNotes()).isEqualTo("Vehicle sold");
        assertThat(result.getStatusId()).isEqualTo(1);
        assertThat(result.getReviewedBy()).isEqualTo(3L);
        assertThat(result.getReviewedAt()).isEqualTo(now);
        assertThat(result.getReviewNotes()).isEqualTo("Approved after verification");
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        PlateChangeRequest result = mapper.toDomain(null);

        assertThat(result).isNull();
    }

    @Test
    void toDomain_shouldMapCorrectly_whenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        PlateChangeRequestEntity entity = PlateChangeRequestEntity.builder()
                .id(2L)
                .subscriptionId(2L)
                .userId(3L)
                .oldLicensePlate("GHI-789")
                .newLicensePlate("JKL-012")
                .reasonId(2)
                .notes("Vehicle theft")
                .statusId(2)
                .reviewedBy(4L)
                .reviewedAt(now)
                .reviewNotes("Approved with police report")
                .createdAt(now)
                .updatedAt(now)
                .build();

        PlateChangeRequest result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getSubscriptionId()).isEqualTo(2L);
        assertThat(result.getUserId()).isEqualTo(3L);
        assertThat(result.getOldLicensePlate()).isEqualTo("GHI-789");
        assertThat(result.getNewLicensePlate()).isEqualTo("JKL-012");
        assertThat(result.getReasonId()).isEqualTo(2);
        assertThat(result.getNotes()).isEqualTo("Vehicle theft");
        assertThat(result.getStatusId()).isEqualTo(2);
        assertThat(result.getReviewedBy()).isEqualTo(4L);
        assertThat(result.getReviewedAt()).isEqualTo(now);
        assertThat(result.getReviewNotes()).isEqualTo("Approved with police report");
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldHandleNullOptionalFields() {
        LocalDateTime now = LocalDateTime.now();
        PlateChangeRequest domain = PlateChangeRequest.builder()
                .id(3L)
                .subscriptionId(3L)
                .userId(4L)
                .oldLicensePlate("MNO-345")
                .newLicensePlate("PQR-678")
                .reasonId(3)
                .notes(null)
                .statusId(1)
                .reviewedBy(null)
                .reviewedAt(null)
                .reviewNotes(null)
                .createdAt(now)
                .updatedAt(null)
                .build();

        PlateChangeRequestEntity result = mapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getNotes()).isNull();
        assertThat(result.getReviewedBy()).isNull();
        assertThat(result.getReviewedAt()).isNull();
        assertThat(result.getReviewNotes()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void toDomain_shouldHandleNullOptionalFields() {
        LocalDateTime now = LocalDateTime.now();
        PlateChangeRequestEntity entity = PlateChangeRequestEntity.builder()
                .id(4L)
                .subscriptionId(4L)
                .userId(5L)
                .oldLicensePlate("STU-901")
                .newLicensePlate("VWX-234")
                .reasonId(1)
                .notes(null)
                .statusId(1)
                .reviewedBy(null)
                .reviewedAt(null)
                .reviewNotes(null)
                .createdAt(now)
                .updatedAt(null)
                .build();

        PlateChangeRequest result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getNotes()).isNull();
        assertThat(result.getReviewedBy()).isNull();
        assertThat(result.getReviewedAt()).isNull();
        assertThat(result.getReviewNotes()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        LocalDateTime now = LocalDateTime.now();
        PlateChangeRequest originalRequest = PlateChangeRequest.builder()
                .id(5L)
                .subscriptionId(5L)
                .userId(6L)
                .oldLicensePlate("YZA-567")
                .newLicensePlate("BCD-890")
                .reasonId(4)
                .notes("Vehicle accident")
                .statusId(3)
                .reviewedBy(7L)
                .reviewedAt(now)
                .reviewNotes("Rejected due to incomplete documentation")
                .createdAt(now)
                .updatedAt(now)
                .build();

        PlateChangeRequestEntity entity = mapper.toEntity(originalRequest);
        PlateChangeRequest resultRequest = mapper.toDomain(entity);

        assertThat(resultRequest).isNotNull();
        assertThat(resultRequest.getId()).isEqualTo(originalRequest.getId());
        assertThat(resultRequest.getSubscriptionId()).isEqualTo(originalRequest.getSubscriptionId());
        assertThat(resultRequest.getUserId()).isEqualTo(originalRequest.getUserId());
        assertThat(resultRequest.getOldLicensePlate()).isEqualTo(originalRequest.getOldLicensePlate());
        assertThat(resultRequest.getNewLicensePlate()).isEqualTo(originalRequest.getNewLicensePlate());
        assertThat(resultRequest.getReasonId()).isEqualTo(originalRequest.getReasonId());
        assertThat(resultRequest.getNotes()).isEqualTo(originalRequest.getNotes());
        assertThat(resultRequest.getStatusId()).isEqualTo(originalRequest.getStatusId());
        assertThat(resultRequest.getReviewedBy()).isEqualTo(originalRequest.getReviewedBy());
        assertThat(resultRequest.getReviewNotes()).isEqualTo(originalRequest.getReviewNotes());
    }

    @Test
    void toEntity_shouldHandlePendingStatus() {
        LocalDateTime now = LocalDateTime.now();
        PlateChangeRequest domain = PlateChangeRequest.builder()
                .id(6L)
                .subscriptionId(6L)
                .userId(7L)
                .oldLicensePlate("EFG-123")
                .newLicensePlate("HIJ-456")
                .reasonId(1)
                .notes("Pending review")
                .statusId(1)
                .reviewedBy(null)
                .reviewedAt(null)
                .reviewNotes(null)
                .createdAt(now)
                .updatedAt(null)
                .build();

        PlateChangeRequestEntity result = mapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getStatusId()).isEqualTo(1);
        assertThat(result.getReviewedBy()).isNull();
        assertThat(result.getReviewedAt()).isNull();
    }

    @Test
    void toDomain_shouldHandleAllReasons() {
        LocalDateTime now = LocalDateTime.now();

        PlateChangeRequestEntity theftEntity = PlateChangeRequestEntity.builder()
                .id(1L)
                .subscriptionId(1L)
                .userId(1L)
                .oldLicensePlate("AAA-111")
                .newLicensePlate("AAA-112")
                .reasonId(1)
                .notes("Theft")
                .statusId(1)
                .createdAt(now)
                .updatedAt(null)
                .build();

        PlateChangeRequestEntity saleEntity = PlateChangeRequestEntity.builder()
                .id(2L)
                .subscriptionId(2L)
                .userId(2L)
                .oldLicensePlate("BBB-222")
                .newLicensePlate("BBB-223")
                .reasonId(2)
                .notes("Sale")
                .statusId(1)
                .createdAt(now)
                .updatedAt(null)
                .build();

        PlateChangeRequestEntity accidentEntity = PlateChangeRequestEntity.builder()
                .id(3L)
                .subscriptionId(3L)
                .userId(3L)
                .oldLicensePlate("CCC-333")
                .newLicensePlate("CCC-334")
                .reasonId(3)
                .notes("Accident")
                .statusId(1)
                .createdAt(now)
                .updatedAt(null)
                .build();

        PlateChangeRequest theftRequest = mapper.toDomain(theftEntity);
        PlateChangeRequest saleRequest = mapper.toDomain(saleEntity);
        PlateChangeRequest accidentRequest = mapper.toDomain(accidentEntity);

        assertThat(theftRequest.getReasonId()).isEqualTo(1);
        assertThat(saleRequest.getReasonId()).isEqualTo(2);
        assertThat(accidentRequest.getReasonId()).isEqualTo(3);
    }
}
