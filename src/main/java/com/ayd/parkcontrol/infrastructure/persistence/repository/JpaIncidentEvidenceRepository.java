package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.IncidentEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaIncidentEvidenceRepository extends JpaRepository<IncidentEvidenceEntity, Long> {

    List<IncidentEvidenceEntity> findByIncidentId(Long incidentId);

    List<IncidentEvidenceEntity> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);

    List<IncidentEvidenceEntity> findByDocumentTypeId(Integer documentTypeId);

    @Query("SELECT COUNT(e) FROM IncidentEvidenceEntity e WHERE e.incidentId = :incidentId")
    Long countByIncidentId(@Param("incidentId") Long incidentId);

    @Query("SELECT e FROM IncidentEvidenceEntity e WHERE e.incidentId = :incidentId AND e.documentTypeId = :documentTypeId")
    List<IncidentEvidenceEntity> findByIncidentAndDocumentType(@Param("incidentId") Long incidentId,
            @Param("documentTypeId") Integer documentTypeId);
}
