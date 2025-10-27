package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateSubscriptionReportUseCase {

    private final JpaSubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> execute() {
        var subscriptions = subscriptionRepository.findAll();

        // Resumen: total activas, expiradas, próximas a vencer
        long active = subscriptions.stream()
                .filter(sub -> sub.getEndDate() != null && sub.getEndDate().isAfter(LocalDateTime.now()))
                .count();

        long expired = subscriptions.stream()
                .filter(sub -> sub.getEndDate() != null && sub.getEndDate().isBefore(LocalDateTime.now()))
                .count();

        LocalDateTime next30Days = LocalDateTime.now().plusDays(30);
        long expiringSoon = subscriptions.stream()
                .filter(sub -> sub.getEndDate() != null
                        && sub.getEndDate().isAfter(LocalDateTime.now())
                        && sub.getEndDate().isBefore(next30Days))
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("total_subscriptions", subscriptions.size());
        summary.put("active_subscriptions", active);
        summary.put("expired_subscriptions", expired);
        summary.put("expiring_soon", expiringSoon);

        return List.of(summary);
    }
}
