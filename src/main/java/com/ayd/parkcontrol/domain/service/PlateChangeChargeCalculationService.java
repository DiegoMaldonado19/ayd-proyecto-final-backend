package com.ayd.parkcontrol.domain.service;

import com.ayd.parkcontrol.domain.model.validation.AdministrativeCharge;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AdministrativeChargeConfigEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PlateChangeRequestEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAdministrativeChargeConfigRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaChangeRequestStatusRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPlateChangeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlateChangeChargeCalculationService {

    private final JpaPlateChangeRequestRepository jpaPlateChangeRequestRepository;
    private final JpaChangeRequestStatusRepository statusRepository;
    private final JpaAdministrativeChargeConfigRepository chargeConfigRepository;

    public AdministrativeCharge calculateCharge(Long subscriptionId, Integer reasonId, LocalDateTime requestDate) {
        log.debug("Calculating administrative charge for subscription: {}", subscriptionId);

        var approvedStatus = statusRepository.findByCode("APPROVED").orElse(null);
        if (approvedStatus == null) {
            log.warn("APPROVED status not found, returning no charge");
            return AdministrativeCharge.noCharge();
        }

        List<PlateChangeRequestEntity> previousApprovedChanges = jpaPlateChangeRequestRepository
                .findBySubscriptionIdAndStatusIdOrderByReviewedAtDesc(subscriptionId, approvedStatus.getId());

        if (previousApprovedChanges.isEmpty()) {
            log.debug("No previous approved changes found, no charge applied");
            return AdministrativeCharge.noCharge();
        }

        PlateChangeRequestEntity lastApprovedChange = previousApprovedChanges.get(0);
        LocalDateTime lastChangeDate = lastApprovedChange.getReviewedAt();

        if (lastChangeDate == null) {
            log.warn("Last approved change has no reviewed_at date, returning no charge");
            return AdministrativeCharge.noCharge();
        }

        long monthsSinceLastChange = ChronoUnit.MONTHS.between(lastChangeDate, requestDate);
        log.debug("Months since last change: {}", monthsSinceLastChange);

        if (monthsSinceLastChange >= 12) {
            log.debug("More than 12 months since last change, no charge applied");
            return AdministrativeCharge.noCharge();
        }

        if (previousApprovedChanges.size() >= 2) {
            log.debug("Second or more change in less than 12 months, applying higher charge");
            return getChargeByReasonCode("SECOND_CHANGE_YEAR");
        }

        if (monthsSinceLastChange >= 6 && monthsSinceLastChange < 12) {
            log.debug("First change between 6 and 12 months, applying reduced charge");
            return getChargeByReasonCode("FIRST_CHANGE_6_TO_12_MONTHS");
        }

        log.debug("Default: No charge conditions met");
        return AdministrativeCharge.noCharge();
    }

    public AdministrativeCharge calculateChargeForRejectedRequests(Long subscriptionId) {
        log.debug("Calculating charge for rejected requests for subscription: {}", subscriptionId);

        var rejectedStatus = statusRepository.findByCode("REJECTED").orElse(null);
        if (rejectedStatus == null) {
            return AdministrativeCharge.noCharge();
        }

        long rejectedCount = jpaPlateChangeRequestRepository
                .countBySubscriptionIdAndStatusId(subscriptionId, rejectedStatus.getId());

        if (rejectedCount > 2) {
            log.debug("More than 2 rejected requests, applying processing fee");
            return getChargeByReasonCode("REPEATED_REQUESTS");
        }

        return AdministrativeCharge.noCharge();
    }

    private AdministrativeCharge getChargeByReasonCode(String reasonCode) {
        AdministrativeChargeConfigEntity config = chargeConfigRepository
                .findByReasonCodeAndIsActiveTrue(reasonCode)
                .orElse(null);

        if (config == null) {
            log.warn("Charge config not found for reason code: {}", reasonCode);
            return AdministrativeCharge.noCharge();
        }

        return AdministrativeCharge.builder()
                .amount(config.getChargeAmount())
                .reason(config.getDescription())
                .reasonCode(config.getReasonCode())
                .build();
    }

    public AdministrativeCharge combineCharges(AdministrativeCharge charge1, AdministrativeCharge charge2) {
        if (!charge1.hasCharge()) {
            return charge2;
        }
        if (!charge2.hasCharge()) {
            return charge1;
        }

        BigDecimal totalAmount = charge1.getAmount().add(charge2.getAmount());
        String combinedReason = charge1.getReason() + " + " + charge2.getReason();

        return AdministrativeCharge.builder()
                .amount(totalAmount)
                .reason(combinedReason)
                .reasonCode("COMBINED_CHARGES")
                .build();
    }
}
