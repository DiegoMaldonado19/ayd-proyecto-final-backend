package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.mapper.TemporalPermitDtoMapper;
import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
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
public class ListTemporalPermitsBySubscriptionUseCase {

    private final JpaTemporalPermitRepository temporalPermitRepository;
    private final JpaSubscriptionRepository subscriptionRepository;
    private final JpaUserRepository userRepository;
    private final TemporalPermitDtoMapper mapper;

    @Transactional(readOnly = true)
    public List<TemporalPermitResponse> execute(Long subscriptionId) {
        log.info("Listing temporal permits for subscription: {}", subscriptionId);

        subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));

        List<TemporalPermitEntity> permits = temporalPermitRepository
                .findBySubscriptionIdOrderByCreatedAtDesc(subscriptionId);

        return permits.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TemporalPermitResponse mapToResponse(TemporalPermitEntity entity) {
        UserEntity approver = userRepository.findById(entity.getApprovedBy()).orElse(null);
        return mapper.toResponse(entity, approver);
    }
}
