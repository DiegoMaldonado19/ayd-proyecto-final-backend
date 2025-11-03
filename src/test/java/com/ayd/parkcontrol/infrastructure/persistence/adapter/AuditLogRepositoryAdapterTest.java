package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.audit.AuditLog;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AuditLogEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.AuditLogMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryAdapterTest {

    @Mock
    private JpaAuditLogRepository jpaAuditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogRepositoryAdapter auditLogRepositoryAdapter;

    private AuditLog auditLog;
    private AuditLogEntity auditLogEntity;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setModule("users");

        auditLogEntity = new AuditLogEntity();
        auditLogEntity.setId(1L);
        auditLogEntity.setModule("users");
    }

    @Test
    void save_ShouldMapToEntityCallRepositoryAndMapToDomain() {
        // Arrange
        when(auditLogMapper.toEntity(auditLog)).thenReturn(auditLogEntity);
        when(jpaAuditLogRepository.save(auditLogEntity)).thenReturn(auditLogEntity);
        when(auditLogMapper.toDomain(auditLogEntity)).thenReturn(auditLog);

        // Act
        AuditLog result = auditLogRepositoryAdapter.save(auditLog);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getModule()).isEqualTo("users");
        verify(auditLogMapper).toEntity(auditLog);
        verify(jpaAuditLogRepository).save(auditLogEntity);
        verify(auditLogMapper).toDomain(auditLogEntity);
    }

    @Test
    void findById_WhenAuditLogExists_ShouldReturnMappedAuditLog() {
        // Arrange
        when(jpaAuditLogRepository.findById(1L)).thenReturn(Optional.of(auditLogEntity));
        when(auditLogMapper.toDomain(auditLogEntity)).thenReturn(auditLog);

        // Act
        Optional<AuditLog> result = auditLogRepositoryAdapter.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(jpaAuditLogRepository).findById(1L);
        verify(auditLogMapper).toDomain(auditLogEntity);
    }

    @Test
    void findById_WhenAuditLogDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(jpaAuditLogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<AuditLog> result = auditLogRepositoryAdapter.findById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaAuditLogRepository).findById(999L);
        verify(auditLogMapper, never()).toDomain(any());
    }

    @Test
    void findAll_ShouldReturnPageOfMappedAuditLogs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLogEntity> entityPage = new PageImpl<>(Collections.singletonList(auditLogEntity));
        when(jpaAuditLogRepository.findAll(pageable)).thenReturn(entityPage);
        when(auditLogMapper.toDomain(auditLogEntity)).thenReturn(auditLog);

        // Act
        Page<AuditLog> result = auditLogRepositoryAdapter.findAll(pageable);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(jpaAuditLogRepository).findAll(pageable);
    }

    @Test
    void findByUserId_ShouldReturnUserAuditLogs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLogEntity> entityPage = new PageImpl<>(Collections.singletonList(auditLogEntity));
        when(jpaAuditLogRepository.findByUserId(1L, pageable)).thenReturn(entityPage);
        when(auditLogMapper.toDomain(auditLogEntity)).thenReturn(auditLog);

        // Act
        Page<AuditLog> result = auditLogRepositoryAdapter.findByUserId(1L, pageable);

        // Assert
        assertThat(result).isNotEmpty();
        verify(jpaAuditLogRepository).findByUserId(1L, pageable);
    }

    @Test
    void findByModule_ShouldReturnFilteredAuditLogs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLogEntity> entityPage = new PageImpl<>(Collections.singletonList(auditLogEntity));
        when(jpaAuditLogRepository.findByModule("users", pageable)).thenReturn(entityPage);
        when(auditLogMapper.toDomain(auditLogEntity)).thenReturn(auditLog);

        // Act
        Page<AuditLog> result = auditLogRepositoryAdapter.findByModule("users", pageable);

        // Assert
        assertThat(result).isNotEmpty();
        verify(jpaAuditLogRepository).findByModule("users", pageable);
    }

    @Test
    void findByCreatedAtBetween_ShouldReturnAuditLogsWithinDateRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLogEntity> entityPage = new PageImpl<>(Collections.singletonList(auditLogEntity));
        when(jpaAuditLogRepository.findByCreatedAtBetween(startDate, endDate, pageable))
                .thenReturn(entityPage);
        when(auditLogMapper.toDomain(auditLogEntity)).thenReturn(auditLog);

        // Act
        Page<AuditLog> result = auditLogRepositoryAdapter.findByCreatedAtBetween(startDate, endDate, pageable);

        // Assert
        assertThat(result).isNotEmpty();
        verify(jpaAuditLogRepository).findByCreatedAtBetween(startDate, endDate, pageable);
    }
}
