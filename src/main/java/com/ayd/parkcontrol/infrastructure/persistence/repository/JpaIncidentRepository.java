package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaIncidentRepository extends JpaRepository<IncidentEntity, Long> {

    List<IncidentEntity> findByBranchId(Long branchId);

    List<IncidentEntity> findByIncidentTypeId(Integer incidentTypeId);

    List<IncidentEntity> findByTicketId(Long ticketId);

    List<IncidentEntity> findByLicensePlate(String licensePlate);

    List<IncidentEntity> findByIsResolved(Boolean isResolved);

    List<IncidentEntity> findByBranchIdAndIsResolved(Long branchId, Boolean isResolved);

    List<IncidentEntity> findByIncidentTypeIdAndIsResolved(Integer incidentTypeId, Boolean isResolved);

    @Query("SELECT i FROM IncidentEntity i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    List<IncidentEntity> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM IncidentEntity i WHERE i.branchId = :branchId AND i.createdAt BETWEEN :startDate AND :endDate")
    List<IncidentEntity> findByBranchAndDateRange(@Param("branchId") Long branchId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(i) FROM IncidentEntity i WHERE i.branchId = :branchId AND i.isResolved = false")
    Long countUnresolvedByBranch(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(i) FROM IncidentEntity i WHERE i.isResolved = false")
    Long countAllUnresolved();
}
