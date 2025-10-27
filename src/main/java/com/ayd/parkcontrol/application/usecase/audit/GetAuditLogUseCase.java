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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAuditLogUseCase {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final OperationTypeRepository operationTypeRepository;
    private final AuditLogDtoMapper auditLogDtoMapper;

    @Transactional(readOnly = true)
    public AuditLogResponse execute(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new AuditLogNotFoundException(id));

        User user = auditLog.getUserId() != null
                ? userRepository.findById(auditLog.getUserId()).orElse(null)
                : null;
        OperationType operationType = operationTypeRepository.findById(auditLog.getOperationTypeId()).orElse(null);

        return auditLogDtoMapper.toResponse(auditLog, user, operationType);
    }
}
