package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.audit.AuditLog;
import com.ayd.parkcontrol.domain.repository.AuditLogRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AuditLogEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.AuditLogMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {

    private final JpaAuditLogRepository jpaAuditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogEntity entity = auditLogMapper.toEntity(auditLog);
        AuditLogEntity savedEntity = jpaAuditLogRepository.save(entity);
        return auditLogMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AuditLog> findById(Long id) {
        return jpaAuditLogRepository.findById(id)
                .map(auditLogMapper::toDomain);
    }

    @Override
    public Page<AuditLog> findAll(Pageable pageable) {
        return jpaAuditLogRepository.findAll(pageable)
                .map(auditLogMapper::toDomain);
    }

    @Override
    public Page<AuditLog> findByUserId(Long userId, Pageable pageable) {
        return jpaAuditLogRepository.findByUserId(userId, pageable)
                .map(auditLogMapper::toDomain);
    }

    @Override
    public Page<AuditLog> findByModule(String module, Pageable pageable) {
        return jpaAuditLogRepository.findByModule(module, pageable)
                .map(auditLogMapper::toDomain);
    }

    @Override
    public Page<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return jpaAuditLogRepository.findByCreatedAtBetween(startDate, endDate, pageable)
                .map(auditLogMapper::toDomain);
    }

    @Override
    public List<AuditLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaAuditLogRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate)
                .stream()
                .map(auditLogMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByModuleAndCreatedAtBetween(String module, LocalDateTime startDate,
            LocalDateTime endDate) {
        return jpaAuditLogRepository.findByModuleAndCreatedAtBetween(module, startDate, endDate)
                .stream()
                .map(auditLogMapper::toDomain)
                .collect(Collectors.toList());
    }
}
