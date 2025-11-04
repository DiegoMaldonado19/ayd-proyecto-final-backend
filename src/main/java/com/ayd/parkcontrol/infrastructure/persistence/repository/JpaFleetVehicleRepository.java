package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetVehicleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaFleetVehicleRepository extends JpaRepository<FleetVehicleEntity, Long> {

        @Query("""
                        SELECT fv FROM FleetVehicleEntity fv
                        JOIN FETCH fv.plan sp
                        JOIN FETCH sp.planType spt
                        JOIN FETCH fv.vehicleType vt
                        WHERE fv.id = :id
                        """)
        Optional<FleetVehicleEntity> findByIdWithDetails(@Param("id") Long id);

        @Query("""
                        SELECT fv FROM FleetVehicleEntity fv
                        JOIN FETCH fv.plan sp
                        JOIN FETCH sp.planType spt
                        JOIN FETCH fv.vehicleType vt
                        WHERE fv.companyId = :companyId
                        AND fv.isActive = true
                        ORDER BY fv.licensePlate ASC
                        """)
        List<FleetVehicleEntity> findActiveByCompanyId(@Param("companyId") Long companyId);

        @Query("""
                        SELECT fv FROM FleetVehicleEntity fv
                        JOIN FETCH fv.plan sp
                        JOIN FETCH sp.planType spt
                        JOIN FETCH fv.vehicleType vt
                        WHERE fv.companyId = :companyId
                        ORDER BY fv.licensePlate ASC
                        """)
        Page<FleetVehicleEntity> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

        @Query("""
                        SELECT fv FROM FleetVehicleEntity fv
                        JOIN FETCH fv.plan sp
                        JOIN FETCH sp.planType spt
                        JOIN FETCH fv.vehicleType vt
                        WHERE fv.companyId = :companyId
                        AND fv.isActive = :isActive
                        ORDER BY fv.licensePlate ASC
                        """)
        Page<FleetVehicleEntity> findByCompanyIdAndIsActive(
                        @Param("companyId") Long companyId,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        @Query("""
                        SELECT fv FROM FleetVehicleEntity fv
                        WHERE fv.companyId = :companyId
                        AND fv.licensePlate = :licensePlate
                        """)
        Optional<FleetVehicleEntity> findByCompanyIdAndLicensePlate(
                        @Param("companyId") Long companyId,
                        @Param("licensePlate") String licensePlate);

        @Query("""
                        SELECT COUNT(fv) FROM FleetVehicleEntity fv
                        WHERE fv.companyId = :companyId
                        AND fv.isActive = true
                        """)
        Long countActiveByCompanyId(@Param("companyId") Long companyId);

        long countByCompanyIdAndIsActive(Long companyId, Boolean isActive);

        boolean existsByCompanyIdAndLicensePlate(Long companyId, String licensePlate);

        @Query("""
                        SELECT CASE WHEN COUNT(fv) > 0 THEN true ELSE false END
                        FROM FleetVehicleEntity fv
                        WHERE fv.licensePlate = :licensePlate
                        AND fv.isActive = true
                        """)
        boolean existsActiveLicensePlate(@Param("licensePlate") String licensePlate);
}
