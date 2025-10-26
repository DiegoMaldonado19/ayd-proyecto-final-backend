package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.exception.SubscriptionNotFoundException;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionDtoMapper mapper;

    @Transactional(readOnly = true)
    public SubscriptionResponse execute(Long id) {
        log.debug("Getting subscription with ID: {}", id);

        Subscription subscription = subscriptionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        "Subscription not found with ID: " + id));

        return mapper.toSubscriptionResponse(subscription);
    }
}
