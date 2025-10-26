package com.ayd.parkcontrol.application.usecase.validation;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class GetTemporalPermitUseCase {

    private final JpaTemporalPermitRepository temporalPermitRepository;
    private final JpaUserRepository userRepository;
    private final TemporalPermitDtoMapper mapper;

    @Transactional(readOnly = true)
    public TemporalPermitResponse execute(Long id) {
        log.info("Getting temporal permit with ID: {}", id);

        TemporalPermitEntity permit = temporalPermitRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Temporal permit not found"));

        UserEntity approver = userRepository.findById(permit.getApprovedBy()).orElse(null);

        return mapper.toResponse(permit, approver);
    }
}
