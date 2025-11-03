package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionUsageResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionUsageDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionUsage;
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
public class GetSubscriptionUsageUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionUsageDtoMapper usageMapper;

    @Transactional(readOnly = true)
    public Page<SubscriptionUsageResponse> execute(Long subscriptionId, Pageable pageable) {
        log.info("Retrieving usage history for subscription ID: {}", subscriptionId);

        subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessRuleException("Subscription not found with ID: " + subscriptionId));

        Page<SubscriptionUsage> usagePage = subscriptionRepository.findUsageBySubscriptionId(subscriptionId, pageable);

        return usagePage.map(usageMapper::toResponse);
    }
}
