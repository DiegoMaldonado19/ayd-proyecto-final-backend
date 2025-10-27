package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.OperationTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOperationTypeRepository extends JpaRepository<OperationTypeEntity, Integer> {

    Optional<OperationTypeEntity> findByCode(String code);
}
