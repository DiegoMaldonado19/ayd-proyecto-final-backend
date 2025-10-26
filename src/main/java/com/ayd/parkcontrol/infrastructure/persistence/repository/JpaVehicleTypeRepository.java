package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaVehicleTypeRepository extends JpaRepository<VehicleTypeEntity, Integer> {

    Optional<VehicleTypeEntity> findByCode(String code);

    boolean existsByCode(String code);
}
