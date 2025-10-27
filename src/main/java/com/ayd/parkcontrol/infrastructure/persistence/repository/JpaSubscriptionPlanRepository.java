package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaSubscriptionPlanRepository extends JpaRepository<SubscriptionPlanEntity, Long> {

    Optional<SubscriptionPlanEntity> findByPlanTypeId(Integer planTypeId);

    @RestResource(path = "by-active", rel = "by-active")
    List<SubscriptionPlanEntity> findByIsActive(Boolean isActive);

    @RestResource(path = "by-active-paged", rel = "by-active-paged")
    Page<SubscriptionPlanEntity> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("""
                SELECT sp FROM SubscriptionPlanEntity sp
                JOIN FETCH sp.planType pt
                WHERE sp.isActive = true
                ORDER BY pt.sortOrder ASC
            """)
    List<SubscriptionPlanEntity> findAllActiveOrderedByHierarchy();

    @Query("""
                SELECT sp FROM SubscriptionPlanEntity sp
                JOIN FETCH sp.planType pt
                WHERE sp.id = :id
            """)
    Optional<SubscriptionPlanEntity> findByIdWithPlanType(@Param("id") Long id);

    boolean existsByPlanTypeId(Integer planTypeId);

    boolean existsByPlanTypeIdAndIdNot(Integer planTypeId, Long id);
}
