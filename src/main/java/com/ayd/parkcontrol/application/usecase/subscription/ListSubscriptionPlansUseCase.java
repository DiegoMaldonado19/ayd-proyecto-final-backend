package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListSubscriptionPlansUseCase {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionDtoMapper mapper;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> execute() {
        log.debug("Listing all active subscription plans");

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAllActiveOrderedByHierarchy();

        return plans.stream()
                .map(mapper::toSubscriptionPlanResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionPlanResponse> execute(Boolean isActive, Pageable pageable) {
        log.debug("Listing subscription plans with isActive: {}", isActive);

        Page<SubscriptionPlan> plans = subscriptionPlanRepository.findByIsActive(isActive, pageable);

        return plans.map(mapper::toSubscriptionPlanResponse);
    }
}
