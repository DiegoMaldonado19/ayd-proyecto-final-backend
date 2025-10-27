package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.audit.OperationType;

import java.util.List;
import java.util.Optional;

public interface OperationTypeRepository {

    Optional<OperationType> findById(Integer id);

    Optional<OperationType> findByCode(String code);

    List<OperationType> findAll();
}
