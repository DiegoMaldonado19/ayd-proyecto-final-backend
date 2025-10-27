package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaTemporalPermitRepository extends JpaRepository<TemporalPermitEntity, Long> {

    List<TemporalPermitEntity> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);

    @Query("SELECT tp FROM TemporalPermitEntity tp WHERE tp.statusTypeId = " +
            "(SELECT st.id FROM TemporalPermitStatusTypeEntity st WHERE st.code = 'ACTIVE') " +
            "AND tp.endDate >= :currentDate " +
            "ORDER BY tp.createdAt DESC")
    List<TemporalPermitEntity> findActivePermits(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT tp FROM TemporalPermitEntity tp WHERE tp.temporalPlate = :temporalPlate " +
            "AND tp.statusTypeId = (SELECT st.id FROM TemporalPermitStatusTypeEntity st WHERE st.code = 'ACTIVE') " +
            "AND tp.startDate <= :currentDate AND tp.endDate >= :currentDate")
    Optional<TemporalPermitEntity> findActivePermitByPlate(
            @Param("temporalPlate") String temporalPlate,
            @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT COUNT(tp) > 0 FROM TemporalPermitEntity tp WHERE tp.subscriptionId = :subscriptionId " +
            "AND tp.statusTypeId = (SELECT st.id FROM TemporalPermitStatusTypeEntity st WHERE st.code = 'ACTIVE') " +
            "AND tp.endDate >= :currentDate")
    boolean hasActivePermitForSubscription(
            @Param("subscriptionId") Long subscriptionId,
            @Param("currentDate") LocalDateTime currentDate);
}
