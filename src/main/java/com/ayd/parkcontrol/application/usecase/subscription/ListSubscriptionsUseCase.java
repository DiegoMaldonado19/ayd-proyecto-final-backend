package com.ayd.parkcontrol.application.usecase.subscription;

import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.mapper.SubscriptionDtoMapper;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
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
public class ListSubscriptionsUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionDtoMapper mapper;

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> execute(Pageable pageable) {
        log.debug("Listing all subscriptions");

        Page<Subscription> subscriptions = subscriptionRepository.findAllWithDetails(pageable);

        return subscriptions.map(mapper::toSubscriptionResponse);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> executeByStatus(Integer statusTypeId, Pageable pageable) {
        log.debug("Listing subscriptions by status type ID: {}", statusTypeId);

        Page<Subscription> subscriptions = subscriptionRepository.findByStatusTypeId(statusTypeId, pageable);

        return subscriptions.map(mapper::toSubscriptionResponse);
    }
}
