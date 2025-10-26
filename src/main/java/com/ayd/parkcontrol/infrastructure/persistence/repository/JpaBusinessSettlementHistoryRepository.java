package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.BusinessSettlementHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaBusinessSettlementHistoryRepository extends JpaRepository<BusinessSettlementHistoryEntity, Long> {

    List<BusinessSettlementHistoryEntity> findByBusinessIdOrderBySettledAtDesc(Long businessId);

    List<BusinessSettlementHistoryEntity> findByBranchIdOrderBySettledAtDesc(Long branchId);

    @Query("SELECT bsh FROM BusinessSettlementHistoryEntity bsh WHERE bsh.businessId = :businessId AND bsh.branchId = :branchId ORDER BY bsh.settledAt DESC")
    List<BusinessSettlementHistoryEntity> findByBusinessAndBranchOrderBySettledAtDesc(
            @Param("businessId") Long businessId, @Param("branchId") Long branchId);

    @Query("SELECT bsh FROM BusinessSettlementHistoryEntity bsh WHERE bsh.settledAt BETWEEN :startDate AND :endDate ORDER BY bsh.settledAt DESC")
    List<BusinessSettlementHistoryEntity> findByPeriod(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT bsh FROM BusinessSettlementHistoryEntity bsh WHERE bsh.businessId = :businessId AND bsh.settledAt BETWEEN :startDate AND :endDate ORDER BY bsh.settledAt DESC")
    List<BusinessSettlementHistoryEntity> findByBusinessIdAndPeriod(@Param("businessId") Long businessId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
