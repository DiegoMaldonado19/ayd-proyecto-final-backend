package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.validation.ChangeRequestEvidence;
import com.ayd.parkcontrol.domain.repository.ChangeRequestEvidenceRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.ChangeRequestEvidenceEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.ChangeRequestEvidenceMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaChangeRequestEvidenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChangeRequestEvidenceRepositoryAdapter implements ChangeRequestEvidenceRepository {

    private final JpaChangeRequestEvidenceRepository jpaRepository;
    private final ChangeRequestEvidenceMapper mapper;

    @Override
    public ChangeRequestEvidence save(ChangeRequestEvidence evidence) {
        ChangeRequestEvidenceEntity entity = mapper.toEntity(evidence);
        ChangeRequestEvidenceEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ChangeRequestEvidence> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<ChangeRequestEvidence> findByChangeRequestId(Long changeRequestId) {
        return jpaRepository.findByChangeRequestId(changeRequestId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByChangeRequestId(Long changeRequestId) {
        return jpaRepository.countByChangeRequestId(changeRequestId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
