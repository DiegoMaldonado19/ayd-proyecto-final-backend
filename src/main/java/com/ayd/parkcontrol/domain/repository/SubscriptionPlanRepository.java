package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.subscription.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository {

    SubscriptionPlan save(SubscriptionPlan subscriptionPlan);

    Optional<SubscriptionPlan> findById(Long id);

    Optional<SubscriptionPlan> findByPlanTypeId(Integer planTypeId);

    List<SubscriptionPlan> findAllActive();

    Page<SubscriptionPlan> findByIsActive(Boolean isActive, Pageable pageable);

    List<SubscriptionPlan> findAllActiveOrderedByHierarchy();

    void deleteById(Long id);

    boolean existsByPlanTypeId(Integer planTypeId);

    boolean existsByPlanTypeIdAndIdNot(Integer planTypeId, Long id);
}
