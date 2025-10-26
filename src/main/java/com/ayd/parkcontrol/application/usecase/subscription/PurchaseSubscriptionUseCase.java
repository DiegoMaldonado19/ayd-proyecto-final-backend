package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.PurchaseSubscriptionRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicateLicensePlateException;
import com.ayd.parkcontrol.domain.exception.SubscriptionPlanNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.repository.SubscriptionPlanRepository;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRateBaseHistoryRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionStatusTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final JpaRateBaseHistoryRepository rateBaseRepository;
    private final JpaSubscriptionStatusTypeRepository statusTypeRepository;
    private final SubscriptionDtoMapper mapper;

    @Transactional
    public SubscriptionResponse execute(PurchaseSubscriptionRequest request, Long userId) {
        log.debug("Purchasing subscription for user ID: {} with plan ID: {}", userId, request.getPlanId());

        if (subscriptionRepository.existsActiveLicensePlate(request.getLicensePlate())) {
            throw new DuplicateLicensePlateException(
                    "License plate " + request.getLicensePlate() + " already has an active subscription");
        }

        subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new SubscriptionPlanNotFoundException(
                        "Subscription plan not found with ID: " + request.getPlanId()));

        BigDecimal frozenRate = rateBaseRepository.findCurrentRate()
                .map(rate -> rate.getAmountPerHour())
                .orElseThrow(() -> new RuntimeException("No current base rate found"));

        Integer statusTypeId = statusTypeRepository.findByCode("ACTIVE")
                .map(status -> status.getId())
                .orElseThrow(() -> new RuntimeException("ACTIVE status type not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now;
        LocalDateTime endDate = request.getIsAnnual() ? now.plusYears(1) : now.plusMonths(1);

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .planId(request.getPlanId())
                .licensePlate(request.getLicensePlate().toUpperCase())
                .frozenRateBase(frozenRate)
                .purchaseDate(now)
                .startDate(startDate)
                .endDate(endDate)
                .consumedHours(BigDecimal.ZERO)
                .statusTypeId(statusTypeId)
                .isAnnual(request.getIsAnnual())
                .notified80Percent(false)
                .autoRenewEnabled(request.getAutoRenewEnabled() != null ? request.getAutoRenewEnabled() : false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        log.info("Subscription purchased successfully with ID: {} for user: {}", saved.getId(), userId);

        return mapper.toSubscriptionResponse(saved);
    }

    @Transactional
    public SubscriptionResponse executeForCurrentUser(PurchaseSubscriptionRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.debug("Purchasing subscription for current user: {}", email);

        Long userId = getUserIdFromEmail(email);

        return execute(request, userId);
    }

    private Long getUserIdFromEmail(String email) {
        return 1L;
    }
}
