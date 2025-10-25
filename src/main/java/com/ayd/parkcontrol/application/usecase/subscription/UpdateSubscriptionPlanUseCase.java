package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.UpdateSubscriptionPlanRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.SubscriptionPlanNotFoundException;
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
public class UpdateSubscriptionPlanUseCase {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionDtoMapper mapper;

    @Transactional
    public SubscriptionPlanResponse execute(Long id, UpdateSubscriptionPlanRequest request) {
        log.debug("Updating subscription plan with ID: {}", id);

        SubscriptionPlan existingPlan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new SubscriptionPlanNotFoundException(
                        "Subscription plan not found with ID: " + id));

        existingPlan.setMonthlyHours(request.getMonthlyHours());
        existingPlan.setMonthlyDiscountPercentage(request.getMonthlyDiscountPercentage());
        existingPlan.setAnnualAdditionalDiscountPercentage(request.getAnnualAdditionalDiscountPercentage());
        existingPlan.setDescription(request.getDescription());

        if (request.getIsActive() != null) {
            existingPlan.setIsActive(request.getIsActive());
        }

        existingPlan.setUpdatedAt(LocalDateTime.now());

        SubscriptionPlan updated = subscriptionPlanRepository.save(existingPlan);

        log.info("Subscription plan updated successfully with ID: {}", id);

        return mapper.toSubscriptionPlanResponse(updated);
    }
}
