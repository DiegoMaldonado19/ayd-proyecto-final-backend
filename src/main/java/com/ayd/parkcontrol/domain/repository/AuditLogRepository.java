package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Optional<AuditLog> findById(Long id);

    Page<AuditLog> findAll(Pageable pageable);

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    Page<AuditLog> findByModule(String module, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<AuditLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<AuditLog> findByModuleAndCreatedAtBetween(String module, LocalDateTime startDate, LocalDateTime endDate);
}
