package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.ChangeRequestEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaChangeRequestEvidenceRepository extends JpaRepository<ChangeRequestEvidenceEntity, Long> {

    @Query("SELECT e FROM ChangeRequestEvidenceEntity e WHERE e.changeRequestId = :changeRequestId ORDER BY e.uploadedAt DESC")
    List<ChangeRequestEvidenceEntity> findByChangeRequestId(@Param("changeRequestId") Long changeRequestId);

    @Query("SELECT COUNT(e) FROM ChangeRequestEvidenceEntity e WHERE e.changeRequestId = :changeRequestId")
    long countByChangeRequestId(@Param("changeRequestId") Long changeRequestId);
}
