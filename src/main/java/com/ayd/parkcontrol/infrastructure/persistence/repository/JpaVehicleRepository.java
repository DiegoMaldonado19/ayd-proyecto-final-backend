package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaVehicleRepository extends JpaRepository<VehicleEntity, Long> {

    List<VehicleEntity> findByUserIdAndIsActive(Long userId, Boolean isActive);

    Page<VehicleEntity> findByIsActive(Boolean isActive, Pageable pageable);

    Optional<VehicleEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndLicensePlate(Long userId, String licensePlate);

    boolean existsByUserIdAndLicensePlateAndIdNot(Long userId, String licensePlate, Long id);

    Optional<VehicleEntity> findByLicensePlate(String licensePlate);
}
