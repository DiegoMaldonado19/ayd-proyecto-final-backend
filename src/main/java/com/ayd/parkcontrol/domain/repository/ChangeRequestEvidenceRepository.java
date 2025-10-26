package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.validation.ChangeRequestEvidence;

import java.util.List;
import java.util.Optional;

public interface ChangeRequestEvidenceRepository {

    ChangeRequestEvidence save(ChangeRequestEvidence evidence);

    Optional<ChangeRequestEvidence> findById(Long id);

    List<ChangeRequestEvidence> findByChangeRequestId(Long changeRequestId);

    long countByChangeRequestId(Long changeRequestId);

    void deleteById(Long id);
}
