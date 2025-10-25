package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.RateBaseHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaRateBaseHistoryRepository extends JpaRepository<RateBaseHistoryEntity, Long> {

    @Query("SELECT r FROM RateBaseHistoryEntity r WHERE r.isActive = true ORDER BY r.startDate DESC")
    Optional<RateBaseHistoryEntity> findCurrentRate();

    @Query("SELECT r FROM RateBaseHistoryEntity r ORDER BY r.startDate DESC")
    List<RateBaseHistoryEntity> findAllOrderByStartDateDesc();

    @Query("SELECT r FROM RateBaseHistoryEntity r WHERE r.isActive = true")
    List<RateBaseHistoryEntity> findAllActive();
}
