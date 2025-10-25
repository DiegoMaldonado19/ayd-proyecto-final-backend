package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.CreateSubscriptionPlanRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicatePlanTypeException;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateSubscriptionPlanUseCase {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionDtoMapper mapper;

    @Transactional
    public SubscriptionPlanResponse execute(CreateSubscriptionPlanRequest request) {
        log.debug("Creating subscription plan for plan type ID: {}", request.getPlanTypeId());

        if (subscriptionPlanRepository.existsByPlanTypeId(request.getPlanTypeId())) {
            throw new DuplicatePlanTypeException(
                    "A subscription plan already exists for plan type ID: " + request.getPlanTypeId());
        }

        SubscriptionPlan subscriptionPlan = SubscriptionPlan.builder()
                .planTypeId(request.getPlanTypeId())
                .monthlyHours(request.getMonthlyHours())
                .monthlyDiscountPercentage(request.getMonthlyDiscountPercentage())
                .annualAdditionalDiscountPercentage(request.getAnnualAdditionalDiscountPercentage())
                .description(request.getDescription())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        SubscriptionPlan saved = subscriptionPlanRepository.save(subscriptionPlan);

        log.info("Subscription plan created successfully with ID: {}", saved.getId());

        return mapper.toSubscriptionPlanResponse(saved);
    }
}
