package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionBalanceResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import com.ayd.parkcontrol.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetMySubscriptionBalanceUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionDtoMapper subscriptionMapper;

    @Transactional(readOnly = true)
    public SubscriptionBalanceResponse execute() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        log.info("Retrieving subscription balance for user ID: {}", userId);

        Subscription subscription = subscriptionRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new BusinessRuleException("No active subscription found for user"));

        return subscriptionMapper.toBalanceResponse(subscription);
    }
}
