package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.SettlementPeriodTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaSettlementPeriodTypeRepository extends JpaRepository<SettlementPeriodTypeEntity, Integer> {

    Optional<SettlementPeriodTypeEntity> findByCode(String code);
}
