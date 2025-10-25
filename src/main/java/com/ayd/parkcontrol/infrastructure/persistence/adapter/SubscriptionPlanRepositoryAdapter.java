package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import com.ayd.parkcontrol.domain.repository.SubscriptionPlanRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionPlanEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.SubscriptionMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaSubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanRepositoryAdapter implements SubscriptionPlanRepository {

    private final JpaSubscriptionPlanRepository jpaRepository;
    private final SubscriptionMapper mapper;

    @Override
    public SubscriptionPlan save(SubscriptionPlan subscriptionPlan) {
        SubscriptionPlanEntity entity = mapper.toEntity(subscriptionPlan);
        SubscriptionPlanEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SubscriptionPlan> findById(Long id) {
        return jpaRepository.findByIdWithPlanType(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<SubscriptionPlan> findByPlanTypeId(Integer planTypeId) {
        return jpaRepository.findByPlanTypeId(planTypeId)
                .map(mapper::toDomain);
    }

    @Override
    public List<SubscriptionPlan> findAllActive() {
        return jpaRepository.findByIsActive(true)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SubscriptionPlan> findByIsActive(Boolean isActive, Pageable pageable) {
        return jpaRepository.findByIsActive(isActive, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<SubscriptionPlan> findAllActiveOrderedByHierarchy() {
        return jpaRepository.findAllActiveOrderedByHierarchy()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByPlanTypeId(Integer planTypeId) {
        return jpaRepository.existsByPlanTypeId(planTypeId);
    }

    @Override
    public boolean existsByPlanTypeIdAndIdNot(Integer planTypeId, Long id) {
        return jpaRepository.existsByPlanTypeIdAndIdNot(planTypeId, id);
    }
}
