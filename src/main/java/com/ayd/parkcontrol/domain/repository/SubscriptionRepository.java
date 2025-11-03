package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionOverage;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {

    Subscription save(Subscription subscription);

    Optional<Subscription> findById(Long id);

    Optional<Subscription> findByIdWithDetails(Long id);

    List<Subscription> findByUserId(Long userId);

    Optional<Subscription> findActiveByUserId(Long userId);

    Optional<Subscription> findActiveLicensePlate(String licensePlate);

    Page<Subscription> findByStatusTypeId(Integer statusTypeId, Pageable pageable);

    Page<Subscription> findAllWithDetails(Pageable pageable);

    List<Subscription> findExpiringSoonWithAutoRenew(LocalDateTime startDate, LocalDateTime endDate);

    void deleteById(Long id);

    boolean existsActiveLicensePlate(String licensePlate);

    boolean existsActiveLicensePlateExcluding(String licensePlate, Long excludeId);

    List<SubscriptionUsage> findUsageBySubscriptionId(Long subscriptionId);

    Page<SubscriptionUsage> findUsageBySubscriptionId(Long subscriptionId, Pageable pageable);

    List<SubscriptionOverage> findOveragesBySubscriptionId(Long subscriptionId);

    Page<SubscriptionOverage> findOveragesBySubscriptionId(Long subscriptionId, Pageable pageable);
}
