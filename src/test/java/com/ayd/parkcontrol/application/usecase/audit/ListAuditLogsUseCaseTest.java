package com.ayd.parkcontrol.application.usecase.audit;

import com.ayd.parkcontrol.application.dto.response.audit.AuditLogResponse;
import com.ayd.parkcontrol.application.mapper.AuditLogDtoMapper;
import com.ayd.parkcontrol.domain.model.audit.AuditLog;
import com.ayd.parkcontrol.domain.repository.AuditLogRepository;
import com.ayd.parkcontrol.domain.repository.OperationTypeRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
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
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListAuditLogsUseCaseTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OperationTypeRepository operationTypeRepository;

    @Mock
    private AuditLogDtoMapper auditLogDtoMapper;

    @InjectMocks
    private ListAuditLogsUseCase listAuditLogsUseCase;

    private AuditLog mockAuditLog;
    private AuditLogResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockAuditLog = AuditLog.builder()
                .id(1L)
                .userId(1L)
                .module("usuarios")
                .entity("User")
                .operationTypeId(1)
                .description("Usuario creado")
                .createdAt(LocalDateTime.now())
                .build();

        mockResponse = AuditLogResponse.builder()
                .id(1L)
                .module("usuarios")
                .build();
    }

    @Test
    void execute_shouldReturnPageOfAuditLogs() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<AuditLog> auditLogPage = new PageImpl<>(Arrays.asList(mockAuditLog));

        when(auditLogRepository.findAll(pageable)).thenReturn(auditLogPage);
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        when(operationTypeRepository.findById(any())).thenReturn(Optional.empty());
        when(auditLogDtoMapper.toResponse(any(), any(), any())).thenReturn(mockResponse);

        Page<AuditLogResponse> result = listAuditLogsUseCase.execute(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(auditLogRepository).findAll(pageable);
    }
}
