package com.ayd.parkcontrol.application.usecase.audit;

import com.ayd.parkcontrol.application.dto.response.audit.AuditLogResponse;
import com.ayd.parkcontrol.application.mapper.AuditLogDtoMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListAuditLogsByDateRangeUseCaseTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OperationTypeRepository operationTypeRepository;

    @Mock
    private AuditLogDtoMapper auditLogDtoMapper;

    @InjectMocks
    private ListAuditLogsByDateRangeUseCase useCase;

    private AuditLog auditLog;
    private User user;
    private OperationType operationType;
    private AuditLogResponse response;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        auditLog = AuditLog.builder()
                .id(1L)
                .userId(1L)
                .operationTypeId(1)
                .module("USER")
                .description("User created")
                .createdAt(LocalDateTime.now())
                .build();

        user = User.builder()
                .id(1L)
                .email("user@test.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        operationType = OperationType.builder()
                .id(1)
                .code("CREATE")
                .name("Create")
                .build();

        response = AuditLogResponse.builder()
                .id(1L)
                .module("USER")
                .description("User created")
                .userEmail("user@test.com")
                .operationType("Create")
                .build();
    }

    @Test
    void execute_shouldReturnAuditLogsByDateRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        Page<AuditLog> auditLogs = new PageImpl<>(List.of(auditLog));

        when(auditLogRepository.findByCreatedAtBetween(eq(startDate), eq(endDate), eq(pageable)))
                .thenReturn(auditLogs);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(operationTypeRepository.findById(1)).thenReturn(Optional.of(operationType));
        when(auditLogDtoMapper.toResponse(eq(auditLog), eq(user), eq(operationType)))
                .thenReturn(response);

        // Act
        Page<AuditLogResponse> result = useCase.execute(startDate, endDate, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(response, result.getContent().get(0));
        verify(auditLogRepository).findByCreatedAtBetween(startDate, endDate, pageable);
    }

    @Test
    void execute_shouldHandleNullUser() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        AuditLog auditLogWithoutUser = AuditLog.builder()
                .id(1L)
                .userId(null)
                .operationTypeId(1)
                .module("SYSTEM")
                .description("System sync")
                .createdAt(LocalDateTime.now())
                .build();

        Page<AuditLog> auditLogs = new PageImpl<>(List.of(auditLogWithoutUser));

        when(auditLogRepository.findByCreatedAtBetween(eq(startDate), eq(endDate), eq(pageable)))
                .thenReturn(auditLogs);
        when(operationTypeRepository.findById(1)).thenReturn(Optional.of(operationType));
        when(auditLogDtoMapper.toResponse(eq(auditLogWithoutUser), eq(null), eq(operationType)))
                .thenReturn(response);

        // Act
        Page<AuditLogResponse> result = useCase.execute(startDate, endDate, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void execute_shouldReturnEmptyPageWhenNoResults() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        Page<AuditLog> emptyPage = new PageImpl<>(List.of());

        when(auditLogRepository.findByCreatedAtBetween(eq(startDate), eq(endDate), eq(pageable)))
                .thenReturn(emptyPage);

        // Act
        Page<AuditLogResponse> result = useCase.execute(startDate, endDate, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
