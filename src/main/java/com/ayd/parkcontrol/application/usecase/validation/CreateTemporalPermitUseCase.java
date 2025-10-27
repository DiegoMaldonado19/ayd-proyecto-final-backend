package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.request.validation.CreateTemporalPermitRequest;
import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTemporalPermitUseCase {

    private final JpaTemporalPermitRepository temporalPermitRepository;
    private final JpaSubscriptionRepository subscriptionRepository;
    private final JpaUserRepository userRepository;
    private final JpaTemporalPermitStatusTypeRepository statusRepository;
    private final JpaVehicleTypeRepository vehicleTypeRepository;
    private final TemporalPermitDtoMapper mapper;

    @Transactional
    public TemporalPermitResponse execute(CreateTemporalPermitRequest request) {
        log.info("Creating temporal permit for subscription: {}", request.getSubscriptionId());

        SubscriptionEntity subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException(request.getSubscriptionId()));

        validateSubscriptionIsActive(subscription);
        validatePermitDates(request.getStartDate(), request.getEndDate());
        validateNoActivePermit(request.getSubscriptionId());
        validatePlateFormat(request.getTemporalPlate());

        vehicleTypeRepository.findById(request.getVehicleTypeId())
                .orElseThrow(() -> new BusinessRuleException("Invalid vehicle type"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("Approver not found"));

        var activeStatus = statusRepository.findByCode("ACTIVE")
                .orElseThrow(() -> new BusinessRuleException("Active status not found"));

        String allowedBranchesJson = mapper.serializeAllowedBranches(request.getAllowedBranches());

        TemporalPermitEntity entity = TemporalPermitEntity.builder()
                .subscriptionId(request.getSubscriptionId())
                .temporalPlate(request.getTemporalPlate().toUpperCase())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxUses(request.getMaxUses())
                .currentUses(0)
                .allowedBranches(allowedBranchesJson)
                .vehicleTypeId(request.getVehicleTypeId())
                .statusTypeId(activeStatus.getId())
                .approvedBy(approver.getId())
                .build();

        TemporalPermitEntity saved = temporalPermitRepository.save(entity);

        TemporalPermitEntity loaded = temporalPermitRepository.findById(saved.getId())
                .orElseThrow(() -> new BusinessRuleException("Failed to load created permit"));

        log.info("Temporal permit created with ID: {}", saved.getId());

        return mapper.toResponse(loaded, approver);
    }

    private void validateSubscriptionIsActive(SubscriptionEntity subscription) {
        if (subscription.getStatus() == null || !"ACTIVE".equals(subscription.getStatus().getCode())) {
            throw new BusinessRuleException("Subscription must be active to create a temporal permit");
        }
    }

    private void validatePermitDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new BusinessRuleException("End date must be after start date");
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 30) {
            throw new BusinessRuleException("Permit duration cannot exceed 30 days");
        }
    }

    private void validateNoActivePermit(Long subscriptionId) {
        boolean hasActive = temporalPermitRepository.hasActivePermitForSubscription(
                subscriptionId, LocalDateTime.now());
        if (hasActive) {
            throw new BusinessRuleException("Subscription already has an active temporal permit");
        }
    }

    private void validatePlateFormat(String plate) {
        if (!plate.matches("^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$")) {
            throw new BusinessRuleException("Invalid temporal plate format");
        }
    }
}
