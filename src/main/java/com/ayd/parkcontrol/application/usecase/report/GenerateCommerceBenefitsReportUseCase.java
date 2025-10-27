package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateCommerceBenefitsReportUseCase {

    private final JpaSubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> execute() {
        // Beneficios comerciales: descuentos, promociones aplicadas
        // Por ahora, generamos un resumen de suscripciones vendidas
        var subscriptions = subscriptionRepository.findAll();

        BigDecimal totalRevenue = subscriptions.stream()
                .map(sub -> sub.getFrozenRateBase() != null ? sub.getFrozenRateBase() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeCount = subscriptions.stream()
                .filter(sub -> sub.getEndDate() != null && sub.getEndDate().isAfter(LocalDateTime.now()))
                .count();

        Map<String, Object> benefits = new HashMap<>();
        benefits.put("total_subscriptions_sold", subscriptions.size());
        benefits.put("active_subscriptions", activeCount);
        benefits.put("total_revenue_from_subscriptions", totalRevenue);

        return List.of(benefits);
    }
}
