package com.ayd.parkcontrol.application.usecase.subscription;

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
public class DeleteSubscriptionPlanUseCase {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional
    public void execute(Long id) {
        log.debug("Soft deleting subscription plan with ID: {}", id);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new SubscriptionPlanNotFoundException(
                        "Subscription plan not found with ID: " + id));

        plan.setIsActive(false);
        subscriptionPlanRepository.save(plan);

        log.info("Subscription plan soft deleted successfully with ID: {}", id);
    }
}
