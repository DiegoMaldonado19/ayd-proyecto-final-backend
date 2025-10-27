package com.ayd.parkcontrol.application.usecase.audit;

import com.ayd.parkcontrol.application.dto.response.audit.AuditLogResponse;
import com.ayd.parkcontrol.application.mapper.AuditLogDtoMapper;
import com.ayd.parkcontrol.domain.exception.AuditLogNotFoundException;
import com.ayd.parkcontrol.domain.model.audit.AuditLog;
import com.ayd.parkcontrol.domain.model.audit.OperationType;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.AuditLogRepository;
import com.ayd.parkcontrol.domain.repository.OperationTypeRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAuditLogUseCaseTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OperationTypeRepository operationTypeRepository;

    @Mock
    private AuditLogDtoMapper auditLogDtoMapper;

    @InjectMocks
    private GetAuditLogUseCase getAuditLogUseCase;

    private AuditLog mockAuditLog;
    private User mockUser;
    private OperationType mockOperationType;
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
                .clientIp("192.168.1.1")
                .createdAt(LocalDateTime.now())
                .build();

        mockUser = User.builder()
                .id(1L)
                .email("admin@parkcontrol.com")
                .build();

        mockOperationType = OperationType.builder()
                .id(1)
                .code("INSERT")
                .name("Insercion")
                .build();

        mockResponse = AuditLogResponse.builder()
                .id(1L)
                .userId(1L)
                .userEmail("admin@parkcontrol.com")
                .module("usuarios")
                .entity("User")
                .operationType("Insercion")
                .description("Usuario creado")
                .clientIp("192.168.1.1")
                .createdAt(mockAuditLog.getCreatedAt())
                .build();
    }

    @Test
    void execute_shouldReturnAuditLog_whenAuditLogExists() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(mockAuditLog));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(operationTypeRepository.findById(1)).thenReturn(Optional.of(mockOperationType));
        when(auditLogDtoMapper.toResponse(any(), any(), any())).thenReturn(mockResponse);

        AuditLogResponse result = getAuditLogUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserEmail()).isEqualTo("admin@parkcontrol.com");
        assertThat(result.getModule()).isEqualTo("usuarios");

        verify(auditLogRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(operationTypeRepository).findById(1);
        verify(auditLogDtoMapper).toResponse(mockAuditLog, mockUser, mockOperationType);
    }

    @Test
    void execute_shouldThrowException_whenAuditLogNotFound() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getAuditLogUseCase.execute(1L))
                .isInstanceOf(AuditLogNotFoundException.class);

        verify(auditLogRepository).findById(1L);
        verify(userRepository, never()).findById(any());
        verify(operationTypeRepository, never()).findById(any());
    }

    @Test
    void execute_shouldHandleNullUser_whenUserNotFound() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(mockAuditLog));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(operationTypeRepository.findById(1)).thenReturn(Optional.of(mockOperationType));
        when(auditLogDtoMapper.toResponse(any(), any(), any())).thenReturn(mockResponse);

        AuditLogResponse result = getAuditLogUseCase.execute(1L);

        assertThat(result).isNotNull();
        verify(auditLogDtoMapper).toResponse(mockAuditLog, null, mockOperationType);
    }
}
