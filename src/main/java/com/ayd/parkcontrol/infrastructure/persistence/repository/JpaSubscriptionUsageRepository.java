package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionUsageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSubscriptionUsageRepository extends JpaRepository<SubscriptionUsageEntity, Long> {

        @Query("SELECT t FROM SubscriptionUsageEntity t " +
                        "WHERE t.subscriptionId = :subscriptionId " +
                        "AND t.exitTime IS NOT NULL " +
                        "ORDER BY t.exitTime DESC")
        List<SubscriptionUsageEntity> findBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

        @Query("SELECT t FROM SubscriptionUsageEntity t " +
                        "WHERE t.subscriptionId = :subscriptionId " +
                        "AND t.exitTime IS NOT NULL " +
                        "ORDER BY t.exitTime DESC")
        Page<SubscriptionUsageEntity> findBySubscriptionIdWithDetails(
                        @Param("subscriptionId") Long subscriptionId,
                        Pageable pageable);
}
