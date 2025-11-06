package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.PlateChangeRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaPlateChangeRequestRepository extends JpaRepository<PlateChangeRequestEntity, Long> {

    @Query("SELECT p FROM PlateChangeRequestEntity p WHERE p.statusId = :statusId ORDER BY p.createdAt DESC")
    List<PlateChangeRequestEntity> findByStatusId(@Param("statusId") Integer statusId);

    @Query("SELECT p FROM PlateChangeRequestEntity p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    List<PlateChangeRequestEntity> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM PlateChangeRequestEntity p WHERE p.subscriptionId = :subscriptionId ORDER BY p.createdAt DESC")
    List<PlateChangeRequestEntity> findBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    @Query("SELECT p FROM PlateChangeRequestEntity p WHERE p.oldLicensePlate = :plate OR p.newLicensePlate = :plate ORDER BY p.createdAt DESC")
    List<PlateChangeRequestEntity> findByLicensePlate(@Param("plate") String plate);

    @Query("SELECT COUNT(p) > 0 FROM PlateChangeRequestEntity p WHERE p.subscriptionId = :subscriptionId AND p.statusId = 1")
    boolean hasActivePendingRequest(@Param("subscriptionId") Long subscriptionId);

    @Query("SELECT p FROM PlateChangeRequestEntity p ORDER BY p.createdAt DESC")
    List<PlateChangeRequestEntity> findAllOrderByCreatedAtDesc();

    @Query("SELECT p FROM PlateChangeRequestEntity p WHERE p.subscriptionId = :subscriptionId AND p.statusId = :statusId ORDER BY p.reviewedAt DESC")
    List<PlateChangeRequestEntity> findBySubscriptionIdAndStatusIdOrderByReviewedAtDesc(
            @Param("subscriptionId") Long subscriptionId,
            @Param("statusId") Integer statusId);

    @Query("SELECT COUNT(p) FROM PlateChangeRequestEntity p WHERE p.subscriptionId = :subscriptionId AND p.statusId = :statusId")
    long countBySubscriptionIdAndStatusId(
            @Param("subscriptionId") Long subscriptionId,
            @Param("statusId") Integer statusId);
}
