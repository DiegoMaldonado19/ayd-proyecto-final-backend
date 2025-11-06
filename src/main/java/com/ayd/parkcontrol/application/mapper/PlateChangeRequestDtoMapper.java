package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.request.validation.CreatePlateChangeRequest;
import com.ayd.parkcontrol.application.dto.response.validation.EvidenceResponse;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
import com.ayd.parkcontrol.domain.model.validation.ChangeRequestEvidence;
import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlateChangeRequestDtoMapper {

        public PlateChangeRequest toDomain(CreatePlateChangeRequest request) {
                return PlateChangeRequest.builder()
                                .subscriptionId(request.getSubscription_id())
                                .userId(request.getUser_id())
                                .oldLicensePlate(request.getOld_license_plate())
                                .newLicensePlate(request.getNew_license_plate())
                                .reasonId(request.getReason_id())
                                .notes(request.getNotes())
                                .statusId(1)
                                .build();
        }

        public PlateChangeRequestResponse toResponse(PlateChangeRequest request, String userName, String reasonName,
                        String statusCode, String statusName, String reviewerName, Long evidenceCount) {
                return PlateChangeRequestResponse.builder()
                                .id(request.getId())
                                .subscription_id(request.getSubscriptionId())
                                .user_id(request.getUserId())
                                .user_name(userName)
                                .old_license_plate(request.getOldLicensePlate())
                                .new_license_plate(request.getNewLicensePlate())
                                .reason_id(request.getReasonId())
                                .reason_name(reasonName)
                                .notes(request.getNotes())
                                .status_id(request.getStatusId())
                                .status_code(statusCode)
                                .status_name(statusName)
                                .reviewed_by(request.getReviewedBy())
                                .reviewer_name(reviewerName)
                                .reviewed_at(request.getReviewedAt())
                                .review_notes(request.getReviewNotes())
                                .created_at(request.getCreatedAt())
                                .updated_at(request.getUpdatedAt())
                                .evidence_count(evidenceCount)
                                .has_administrative_charge(request.getHasAdministrativeCharge())
                                .administrative_charge_amount(request.getAdministrativeChargeAmount())
                                .administrative_charge_reason(request.getAdministrativeChargeReason())
                                .build();
        }

        public PlateChangeRequestResponse toResponse(PlateChangeRequest request, String userName, String reasonName,
                        String statusCode, String statusName, String reviewerName, List<EvidenceResponse> evidences) {
                return PlateChangeRequestResponse.builder()
                                .id(request.getId())
                                .subscription_id(request.getSubscriptionId())
                                .user_id(request.getUserId())
                                .user_name(userName)
                                .old_license_plate(request.getOldLicensePlate())
                                .new_license_plate(request.getNewLicensePlate())
                                .reason_id(request.getReasonId())
                                .reason_name(reasonName)
                                .notes(request.getNotes())
                                .status_id(request.getStatusId())
                                .status_code(statusCode)
                                .status_name(statusName)
                                .reviewed_by(request.getReviewedBy())
                                .reviewer_name(reviewerName)
                                .reviewed_at(request.getReviewedAt())
                                .review_notes(request.getReviewNotes())
                                .created_at(request.getCreatedAt())
                                .updated_at(request.getUpdatedAt())
                                .evidence_count(evidences != null ? (long) evidences.size() : 0L)
                                .evidences(evidences)
                                .has_administrative_charge(request.getHasAdministrativeCharge())
                                .administrative_charge_amount(request.getAdministrativeChargeAmount())
                                .administrative_charge_reason(request.getAdministrativeChargeReason())
                                .build();
        }

        public EvidenceResponse toEvidenceResponse(ChangeRequestEvidence evidence, String documentTypeCode,
                        String documentTypeName) {
                return EvidenceResponse.builder()
                                .id(evidence.getId())
                                .change_request_id(evidence.getChangeRequestId())
                                .document_type_id(evidence.getDocumentTypeId())
                                .document_type_code(documentTypeCode)
                                .document_type_name(documentTypeName)
                                .file_name(evidence.getFileName())
                                .file_url(evidence.getFileUrl())
                                .file_size(evidence.getFileSize())
                                .uploaded_by(evidence.getUploadedBy())
                                .uploaded_at(evidence.getUploadedAt())
                                .build();
        }
}
