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
        LocalDateTime newStartDate;
        LocalDateTime newEndDate;

        // Si la suscripción ya venció, el nuevo periodo empieza ahora
        // Si aún está activa, el nuevo periodo empieza cuando termine la actual
        if (subscription.getEndDate().isBefore(now)) {
            newStartDate = now;
        } else {
            newStartDate = subscription.getEndDate();
        }

        // Calcular fecha de fin según el tipo de renovación
        if (Boolean.TRUE.equals(request.getIsAnnual())) {
            newEndDate = newStartDate.plusYears(1);
        } else {
            newEndDate = newStartDate.plusMonths(1);
        }

        subscription.setConsumedHours(BigDecimal.ZERO);
        subscription.setNotified80Percent(false);
        subscription.setIsAnnual(request.getIsAnnual());
        subscription.setAutoRenewEnabled(request.getAutoRenewEnabled() != null ? request.getAutoRenewEnabled() : false);
        subscription.setPurchaseDate(now);
        subscription.setStartDate(newStartDate);
        subscription.setEndDate(newEndDate);
        subscription.setUpdatedAt(now);

        Subscription renewed = subscriptionRepository.save(subscription);

        log.info("Subscription renewed successfully: {} - New period: {} to {}", 
                subscriptionId, newStartDate, newEndDate);

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
