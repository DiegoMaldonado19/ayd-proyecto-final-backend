package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionOverageResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionUsageDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionOverage;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetSubscriptionOveragesUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionUsageDtoMapper usageMapper;

    @Transactional(readOnly = true)
    public Page<SubscriptionOverageResponse> execute(Long subscriptionId, Pageable pageable) {
        log.info("Retrieving overages for subscription ID: {}", subscriptionId);

        subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessRuleException("Subscription not found with ID: " + subscriptionId));

        Page<SubscriptionOverage> overagePage = subscriptionRepository.findOveragesBySubscriptionId(subscriptionId,
                pageable);

        return overagePage.map(usageMapper::toOverageResponse);
    }
}
