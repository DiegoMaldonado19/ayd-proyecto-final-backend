package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.IncidentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaIncidentTypeRepository extends JpaRepository<IncidentTypeEntity, Integer> {

    Optional<IncidentTypeEntity> findByCode(String code);

    boolean existsByCode(String code);
}
