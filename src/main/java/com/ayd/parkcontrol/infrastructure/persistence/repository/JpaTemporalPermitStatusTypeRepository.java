package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.TemporalPermitStatusTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaTemporalPermitStatusTypeRepository extends JpaRepository<TemporalPermitStatusTypeEntity, Integer> {

    Optional<TemporalPermitStatusTypeEntity> findByCode(String code);
}
