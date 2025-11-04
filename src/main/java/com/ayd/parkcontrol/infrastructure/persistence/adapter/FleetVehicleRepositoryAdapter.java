package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.fleet.FleetVehicle;
import com.ayd.parkcontrol.domain.repository.FleetVehicleRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.FleetVehicleEntityMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetVehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FleetVehicleRepositoryAdapter implements FleetVehicleRepository {

    private final JpaFleetVehicleRepository jpaFleetVehicleRepository;
    private final FleetVehicleEntityMapper fleetVehicleEntityMapper;

    @Override
    public Page<FleetVehicle> findByCompanyId(Long companyId, Pageable pageable) {
        Page<FleetVehicleEntity> entityPage = jpaFleetVehicleRepository.findByCompanyId(companyId, pageable);
        return entityPage.map(fleetVehicleEntityMapper::toDomain);
    }

    @Override
    public Page<FleetVehicle> findByCompanyIdAndIsActive(Long companyId, Boolean isActive, Pageable pageable) {
        Page<FleetVehicleEntity> entityPage = jpaFleetVehicleRepository.findByCompanyIdAndIsActive(companyId, isActive,
                pageable);
        return entityPage.map(fleetVehicleEntityMapper::toDomain);
    }

    @Override
    public long countByCompanyIdAndIsActive(Long companyId, Boolean isActive) {
        return jpaFleetVehicleRepository.countByCompanyIdAndIsActive(companyId, isActive);
    }

    @Override
    public boolean existsByCompanyIdAndLicensePlate(Long companyId, String licensePlate) {
        return jpaFleetVehicleRepository.existsByCompanyIdAndLicensePlate(companyId, licensePlate);
    }

    @Override
    public FleetVehicle save(FleetVehicle fleetVehicle) {
        FleetVehicleEntity entity = fleetVehicleEntityMapper.toEntity(fleetVehicle);
        FleetVehicleEntity savedEntity = jpaFleetVehicleRepository.save(entity);
        return fleetVehicleEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<FleetVehicle> findById(Long id) {
        return jpaFleetVehicleRepository.findById(id)
                .map(fleetVehicleEntityMapper::toDomain);
    }
}
