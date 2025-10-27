package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTemporalPermitRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListTemporalPermitsUseCase {

    private final JpaTemporalPermitRepository temporalPermitRepository;
    private final JpaUserRepository userRepository;
    private final TemporalPermitDtoMapper mapper;

    @Transactional(readOnly = true)
    public List<TemporalPermitResponse> execute() {
        log.info("Listing all temporal permits");

        List<TemporalPermitEntity> permits = temporalPermitRepository.findAll();

        return permits.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TemporalPermitResponse mapToResponse(TemporalPermitEntity entity) {
        UserEntity approver = userRepository.findById(entity.getApprovedBy()).orElse(null);
        return mapper.toResponse(entity, approver);
    }
}
