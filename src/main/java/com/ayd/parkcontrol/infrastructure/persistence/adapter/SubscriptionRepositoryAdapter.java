package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionOverage;
import com.ayd.parkcontrol.domain.model.subscription.SubscriptionUsage;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.SubscriptionMapper;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.SubscriptionUsageMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionOverageRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final JpaSubscriptionRepository jpaRepository;
    private final JpaSubscriptionUsageRepository usageRepository;
    private final JpaSubscriptionOverageRepository overageRepository;
    private final SubscriptionMapper mapper;
    private final SubscriptionUsageMapper usageMapper;

    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionEntity entity = mapper.toEntity(subscription);
        SubscriptionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(jpaRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    @Override
    public Optional<Subscription> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Subscription> findByIdWithDetails(Long id) {
        return jpaRepository.findByIdWithDetails(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Subscription> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Subscription> findActiveByUserId(Long userId) {
        return jpaRepository.findActiveByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Subscription> findActiveLicensePlate(String licensePlate) {
        return jpaRepository.findActiveLicensePlate(licensePlate, LocalDateTime.now())
                .map(mapper::toDomain);
    }

    @Override
    public Page<Subscription> findByStatusTypeId(Integer statusTypeId, Pageable pageable) {
        return jpaRepository.findByStatusTypeId(statusTypeId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Subscription> findAllWithDetails(Pageable pageable) {
        return jpaRepository.findAllWithDetails(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<Subscription> findExpiringSoonWithAutoRenew(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findExpiringSoonWithAutoRenew(startDate, endDate)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsActiveLicensePlate(String licensePlate) {
        return jpaRepository.existsActiveLicensePlate(licensePlate, LocalDateTime.now());
    }

    @Override
    public boolean existsActiveLicensePlateExcluding(String licensePlate, Long excludeId) {
        return jpaRepository.existsActiveLicensePlateExcluding(licensePlate, excludeId, LocalDateTime.now());
    }

    @Override
    public List<SubscriptionUsage> findUsageBySubscriptionId(Long subscriptionId) {
        return usageRepository.findBySubscriptionId(subscriptionId)
                .stream()
                .map(usageMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SubscriptionUsage> findUsageBySubscriptionId(Long subscriptionId, Pageable pageable) {
        return usageRepository.findBySubscriptionIdWithDetails(subscriptionId, pageable)
                .map(usageMapper::toDomain);
    }

    @Override
    public List<SubscriptionOverage> findOveragesBySubscriptionId(Long subscriptionId) {
        return overageRepository.findBySubscriptionId(subscriptionId)
                .stream()
                .map(usageMapper::toOverageDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SubscriptionOverage> findOveragesBySubscriptionId(Long subscriptionId, Pageable pageable) {
        return overageRepository.findBySubscriptionIdWithDetails(subscriptionId, pageable)
                .map(usageMapper::toOverageDomain);
    }
}
