package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PlateChangeRequestEntity;
import org.springframework.stereotype.Component;

@Component
public class PlateChangeRequestMapper {

    public PlateChangeRequestEntity toEntity(PlateChangeRequest domain) {
        if (domain == null) {
            return null;
        }

        return PlateChangeRequestEntity.builder()
                .id(domain.getId())
                .subscriptionId(domain.getSubscriptionId())
                .userId(domain.getUserId())
                .oldLicensePlate(domain.getOldLicensePlate())
                .newLicensePlate(domain.getNewLicensePlate())
                .reasonId(domain.getReasonId())
                .notes(domain.getNotes())
                .statusId(domain.getStatusId())
                .reviewedBy(domain.getReviewedBy())
                .reviewedAt(domain.getReviewedAt())
                .reviewNotes(domain.getReviewNotes())
                .hasAdministrativeCharge(domain.getHasAdministrativeCharge())
                .administrativeChargeAmount(domain.getAdministrativeChargeAmount())
                .administrativeChargeReason(domain.getAdministrativeChargeReason())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public PlateChangeRequest toDomain(PlateChangeRequestEntity entity) {
        if (entity == null) {
            return null;
        }

        return PlateChangeRequest.builder()
                .id(entity.getId())
                .subscriptionId(entity.getSubscriptionId())
                .userId(entity.getUserId())
                .oldLicensePlate(entity.getOldLicensePlate())
                .newLicensePlate(entity.getNewLicensePlate())
                .reasonId(entity.getReasonId())
                .notes(entity.getNotes())
                .statusId(entity.getStatusId())
                .reviewedBy(entity.getReviewedBy())
                .reviewedAt(entity.getReviewedAt())
                .reviewNotes(entity.getReviewNotes())
                .hasAdministrativeCharge(entity.getHasAdministrativeCharge())
                .administrativeChargeAmount(entity.getAdministrativeChargeAmount())
                .administrativeChargeReason(entity.getAdministrativeChargeReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
