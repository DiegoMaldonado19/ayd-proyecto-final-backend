package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.RenewSubscriptionRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RenewSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionDtoMapper subscriptionMapper;

    @Transactional
    public SubscriptionResponse execute(Long subscriptionId, RenewSubscriptionRequest request) {
        log.info("Renewing subscription with ID: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findByIdWithDetails(subscriptionId)
                .orElseThrow(() -> new BusinessRuleException("Subscription not found with ID: " + subscriptionId));

        validateCanRenew(subscription);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newEndDate;

        if (Boolean.TRUE.equals(request.getIsAnnual())) {
            newEndDate = subscription.getEndDate().plusYears(1);
        } else {
            newEndDate = subscription.getEndDate().plusMonths(1);
        }

        subscription.setConsumedHours(BigDecimal.ZERO);
        subscription.setNotified80Percent(false);
        subscription.setIsAnnual(request.getIsAnnual());
        subscription.setAutoRenewEnabled(request.getAutoRenewEnabled() != null ? request.getAutoRenewEnabled() : false);
        subscription.setEndDate(newEndDate);
        subscription.setPurchaseDate(now);
        subscription.setUpdatedAt(now);

        Subscription renewed = subscriptionRepository.save(subscription);

        log.info("Subscription renewed successfully: {}", subscriptionId);

        return subscriptionMapper.toSubscriptionResponse(renewed);
    }

    private void validateCanRenew(Subscription subscription) {
        if (subscription.getStatusTypeId() == null) {
            throw new BusinessRuleException("Subscription status is undefined");
        }

        if (subscription.getStatusTypeId() != 1) {
            throw new BusinessRuleException(
                    "Only active subscriptions can be renewed. Current status: " + subscription.getStatusName());
        }

        if (subscription.getPlan() == null) {
            throw new BusinessRuleException("Subscription plan information is missing");
        }
    }
}
