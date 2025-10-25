package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.SubscriptionPlanNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetSubscriptionPlanUseCase {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionDtoMapper mapper;

    @Transactional(readOnly = true)
    public SubscriptionPlanResponse execute(Long id) {
        log.debug("Getting subscription plan with ID: {}", id);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new SubscriptionPlanNotFoundException(
                        "Subscription plan not found with ID: " + id));

        return mapper.toSubscriptionPlanResponse(plan);
    }
}
