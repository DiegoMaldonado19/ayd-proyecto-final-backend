package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionPlanTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaSubscriptionPlanTypeRepository extends JpaRepository<SubscriptionPlanTypeEntity, Integer> {

    Optional<SubscriptionPlanTypeEntity> findByCode(String code);

    boolean existsByCode(String code);
}
