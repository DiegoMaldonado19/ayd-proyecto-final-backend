package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.PlateChangeReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaPlateChangeReasonRepository extends JpaRepository<PlateChangeReasonEntity, Integer> {

    Optional<PlateChangeReasonEntity> findByCode(String code);
}
