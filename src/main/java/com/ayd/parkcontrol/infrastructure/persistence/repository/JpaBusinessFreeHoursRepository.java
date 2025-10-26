package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.BusinessFreeHoursEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaBusinessFreeHoursRepository extends JpaRepository<BusinessFreeHoursEntity, Long> {

    List<BusinessFreeHoursEntity> findByTicketId(Long ticketId);

    @Query("SELECT COALESCE(SUM(bfh.grantedHours), 0) FROM BusinessFreeHoursEntity bfh WHERE bfh.ticketId = :ticketId")
    BigDecimal sumGrantedHoursByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT bfh FROM BusinessFreeHoursEntity bfh WHERE bfh.businessId = :businessId AND bfh.branchId = :branchId AND bfh.isSettled = false AND bfh.grantedAt BETWEEN :startDate AND :endDate")
    List<BusinessFreeHoursEntity> findUnsettledByBusinessBranchAndPeriod(@Param("businessId") Long businessId,
            @Param("branchId") Long branchId, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Modifying
    @Query("UPDATE BusinessFreeHoursEntity bfh SET bfh.isSettled = true WHERE bfh.id IN :ids")
    void markAsSettled(@Param("ids") List<Long> ids);
}
