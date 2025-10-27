package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetActiveSubscriptionsCountUseCase {

    private final JpaSubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public Long execute() {
        // Contar suscripciones que no han expirado
        return subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getEndDate() != null && sub.getEndDate().isAfter(LocalDateTime.now()))
                .count();
    }
}
