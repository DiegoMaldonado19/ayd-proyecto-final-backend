package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.AdministrativeChargeConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaAdministrativeChargeConfigRepository
        extends JpaRepository<AdministrativeChargeConfigEntity, Integer> {

    Optional<AdministrativeChargeConfigEntity> findByReasonCode(String reasonCode);

    Optional<AdministrativeChargeConfigEntity> findByReasonCodeAndIsActiveTrue(String reasonCode);
}
