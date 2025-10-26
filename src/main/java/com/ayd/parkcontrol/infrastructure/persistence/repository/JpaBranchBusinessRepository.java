package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchBusinessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaBranchBusinessRepository extends JpaRepository<BranchBusinessEntity, Long> {

    List<BranchBusinessEntity> findByBusinessIdAndIsActiveTrue(Long businessId);

    List<BranchBusinessEntity> findByBranchIdAndIsActiveTrue(Long branchId);

    Optional<BranchBusinessEntity> findByBusinessIdAndBranchId(Long businessId, Long branchId);

    @Query("SELECT bb FROM BranchBusinessEntity bb WHERE bb.businessId = :businessId AND bb.branchId = :branchId AND bb.isActive = true")
    Optional<BranchBusinessEntity> findActiveByBusinessAndBranch(@Param("businessId") Long businessId,
            @Param("branchId") Long branchId);

    boolean existsByBusinessIdAndBranchIdAndIsActiveTrue(Long businessId, Long branchId);
}
