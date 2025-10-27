package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.request.validation.UpdateTemporalPermitRequest;
import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTemporalPermitUseCase {

    private final JpaTemporalPermitRepository temporalPermitRepository;
    private final JpaUserRepository userRepository;
    private final TemporalPermitDtoMapper mapper;

    @Transactional
    public TemporalPermitResponse execute(Long id, UpdateTemporalPermitRequest request) {
        log.info("Updating temporal permit with ID: {}", id);

        TemporalPermitEntity permit = temporalPermitRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Temporal permit not found"));

        validatePermitIsActive(permit);

        if (request.getStartDate() != null) {
            permit.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            permit.setEndDate(request.getEndDate());
        }

        if (permit.getStartDate() != null && permit.getEndDate() != null) {
            validatePermitDates(permit.getStartDate(), permit.getEndDate());
        }

        if (request.getMaxUses() != null) {
            validateMaxUses(request.getMaxUses(), permit.getCurrentUses());
            permit.setMaxUses(request.getMaxUses());
        }

        if (request.getAllowedBranches() != null) {
            String allowedBranchesJson = mapper.serializeAllowedBranches(request.getAllowedBranches());
            permit.setAllowedBranches(allowedBranchesJson);
        }

        TemporalPermitEntity updated = temporalPermitRepository.save(permit);

        TemporalPermitEntity loaded = temporalPermitRepository.findById(updated.getId())
                .orElseThrow(() -> new BusinessRuleException("Failed to load updated permit"));

        UserEntity approver = userRepository.findById(loaded.getApprovedBy()).orElse(null);

        log.info("Temporal permit updated successfully: {}", id);

        return mapper.toResponse(loaded, approver);
    }

    private void validatePermitIsActive(TemporalPermitEntity permit) {
        if (permit.getStatus() == null || !"ACTIVE".equals(permit.getStatus().getCode())) {
            throw new BusinessRuleException("Only active permits can be updated");
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

    private void validateMaxUses(Integer newMaxUses, Integer currentUses) {
        if (newMaxUses < currentUses) {
            throw new BusinessRuleException("Max uses cannot be less than current uses");
        }
    }
}
