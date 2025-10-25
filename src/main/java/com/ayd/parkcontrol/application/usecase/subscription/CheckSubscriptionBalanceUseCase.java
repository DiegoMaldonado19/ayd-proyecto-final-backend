package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionBalanceResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckSubscriptionBalanceUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionDtoMapper mapper;

    @Transactional(readOnly = true)
    public SubscriptionBalanceResponse execute(Long subscriptionId) {
        log.debug("Checking balance for subscription ID: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findByIdWithDetails(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        "Subscription not found with ID: " + subscriptionId));

        return mapper.toBalanceResponse(subscription);
    }
}
