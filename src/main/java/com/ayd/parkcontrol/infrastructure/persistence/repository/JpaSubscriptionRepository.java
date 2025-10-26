package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.SubscriptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaSubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    @Query("""
                SELECT s FROM SubscriptionEntity s
                JOIN FETCH s.plan sp
                JOIN FETCH sp.planType spt
                JOIN FETCH s.status st
                WHERE s.id = :id
            """)
    Optional<SubscriptionEntity> findByIdWithDetails(@Param("id") Long id);

    @Query("""
                SELECT s FROM SubscriptionEntity s
                JOIN FETCH s.plan sp
                JOIN FETCH sp.planType spt
                JOIN FETCH s.status st
                WHERE s.userId = :userId
                ORDER BY s.createdAt DESC
            """)
    List<SubscriptionEntity> findByUserId(@Param("userId") Long userId);

    @Query("""
                SELECT s FROM SubscriptionEntity s
                JOIN FETCH s.plan sp
                JOIN FETCH sp.planType spt
                JOIN FETCH s.status st
                WHERE s.userId = :userId
                AND s.statusTypeId = (SELECT st2.id FROM SubscriptionStatusTypeEntity st2 WHERE st2.code = 'ACTIVE')
            """)
    Optional<SubscriptionEntity> findActiveByUserId(@Param("userId") Long userId);

    @Query("""
                SELECT s FROM SubscriptionEntity s
                WHERE s.licensePlate = :licensePlate
                AND s.statusTypeId = (SELECT st.id FROM SubscriptionStatusTypeEntity st WHERE st.code = 'ACTIVE')
                AND s.endDate > :now
            """)
    Optional<SubscriptionEntity> findActiveLicensePlate(
            @Param("licensePlate") String licensePlate,
            @Param("now") LocalDateTime now);

    @Query("""
                SELECT s FROM SubscriptionEntity s
                JOIN FETCH s.plan sp
                JOIN FETCH sp.planType spt
                JOIN FETCH s.status st
                WHERE s.statusTypeId = :statusTypeId
            """)
    Page<SubscriptionEntity> findByStatusTypeId(@Param("statusTypeId") Integer statusTypeId, Pageable pageable);

    @Query("""
                SELECT s FROM SubscriptionEntity s
                JOIN FETCH s.plan sp
                JOIN FETCH sp.planType spt
                JOIN FETCH s.status st
                JOIN FETCH s.user u
                ORDER BY s.createdAt DESC
            """)
    Page<SubscriptionEntity> findAllWithDetails(Pageable pageable);

    @Query("""
                SELECT s FROM SubscriptionEntity s
                WHERE s.endDate BETWEEN :startDate AND :endDate
                AND s.autoRenewEnabled = true
                AND s.statusTypeId = (SELECT st.id FROM SubscriptionStatusTypeEntity st WHERE st.code = 'ACTIVE')
            """)
    List<SubscriptionEntity> findExpiringSoonWithAutoRenew(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
                SELECT COUNT(s) > 0 FROM SubscriptionEntity s
                WHERE s.licensePlate = :licensePlate
                AND s.statusTypeId = (SELECT st.id FROM SubscriptionStatusTypeEntity st WHERE st.code = 'ACTIVE')
                AND s.endDate > :now
            """)
    boolean existsActiveLicensePlate(
            @Param("licensePlate") String licensePlate,
            @Param("now") LocalDateTime now);

    @Query("""
                SELECT COUNT(s) > 0 FROM SubscriptionEntity s
                WHERE s.licensePlate = :licensePlate
                AND s.id != :excludeId
                AND s.statusTypeId = (SELECT st.id FROM SubscriptionStatusTypeEntity st WHERE st.code = 'ACTIVE')
                AND s.endDate > :now
            """)
    boolean existsActiveLicensePlateExcluding(
            @Param("licensePlate") String licensePlate,
            @Param("excludeId") Long excludeId,
            @Param("now") LocalDateTime now);
}
