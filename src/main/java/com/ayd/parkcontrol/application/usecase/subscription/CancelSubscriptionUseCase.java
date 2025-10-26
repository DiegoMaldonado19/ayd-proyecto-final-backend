package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionStatusTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final JpaSubscriptionStatusTypeRepository statusTypeRepository;

    @Transactional
    public void execute(Long id) {
        log.debug("Cancelling subscription with ID: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        "Subscription not found with ID: " + id));

        Integer cancelledStatusId = statusTypeRepository.findByCode("CANCELLED")
                .map(status -> status.getId())
                .orElseThrow(() -> new RuntimeException("CANCELLED status type not found"));

        subscription.setStatusTypeId(cancelledStatusId);
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled successfully with ID: {}", id);
    }
}
