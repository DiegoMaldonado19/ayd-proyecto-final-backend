package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitStatusTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevokeTemporalPermitUseCase {

    private final JpaTemporalPermitRepository temporalPermitRepository;
    private final JpaTemporalPermitStatusTypeRepository statusRepository;
    private final JpaUserRepository userRepository;
    private final TemporalPermitDtoMapper mapper;

    @Transactional
    public TemporalPermitResponse execute(Long id) {
        log.info("Revoking temporal permit with ID: {}", id);

        TemporalPermitEntity permit = temporalPermitRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Temporal permit not found"));

        validatePermitIsActive(permit);

        TemporalPermitStatusTypeEntity revokedStatus = statusRepository.findByCode("REVOKED")
                .orElseThrow(() -> new BusinessRuleException("Revoked status not found"));

        permit.setStatusTypeId(revokedStatus.getId());

        TemporalPermitEntity updated = temporalPermitRepository.save(permit);

        TemporalPermitEntity loaded = temporalPermitRepository.findById(updated.getId())
                .orElseThrow(() -> new BusinessRuleException("Failed to load revoked permit"));

        UserEntity approver = userRepository.findById(loaded.getApprovedBy()).orElse(null);

        log.info("Temporal permit revoked successfully: {}", id);

        return mapper.toResponse(loaded, approver);
    }

    private void validatePermitIsActive(TemporalPermitEntity permit) {
        if (permit.getStatus() == null || !"ACTIVE".equals(permit.getStatus().getCode())) {
            throw new BusinessRuleException("Only active permits can be revoked");
        }
    }
}
