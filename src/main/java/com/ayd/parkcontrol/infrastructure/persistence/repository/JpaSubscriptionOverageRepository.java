package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionOverageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSubscriptionOverageRepository extends JpaRepository<SubscriptionOverageEntity, Long> {

        @Query("SELECT o FROM SubscriptionOverageEntity o " +
                        "WHERE o.subscriptionId = :subscriptionId " +
                        "ORDER BY o.chargedAt DESC")
        List<SubscriptionOverageEntity> findBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

        @Query("SELECT o FROM SubscriptionOverageEntity o " +
                        "LEFT JOIN FETCH o.ticket t " +
                        "WHERE o.subscriptionId = :subscriptionId " +
                        "ORDER BY o.chargedAt DESC")
        Page<SubscriptionOverageEntity> findBySubscriptionIdWithDetails(
                        @Param("subscriptionId") Long subscriptionId,
                        Pageable pageable);
}
