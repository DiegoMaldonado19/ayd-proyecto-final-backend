package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.audit.OperationType;
import com.ayd.parkcontrol.domain.repository.OperationTypeRepository;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.OperationTypeMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaOperationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OperationTypeRepositoryAdapter implements OperationTypeRepository {

    private final JpaOperationTypeRepository jpaOperationTypeRepository;
    private final OperationTypeMapper operationTypeMapper;

    @Override
    public Optional<OperationType> findById(Integer id) {
        return jpaOperationTypeRepository.findById(id)
                .map(operationTypeMapper::toDomain);
    }

    @Override
    public Optional<OperationType> findByCode(String code) {
        return jpaOperationTypeRepository.findByCode(code)
                .map(operationTypeMapper::toDomain);
    }

    @Override
    public List<OperationType> findAll() {
        return jpaOperationTypeRepository.findAll()
                .stream()
                .map(operationTypeMapper::toDomain)
                .collect(Collectors.toList());
    }
}
