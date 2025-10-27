package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaFleetCompanyRepository extends JpaRepository<FleetCompanyEntity, Long> {

    Optional<FleetCompanyEntity> findByTaxId(String taxId);

    boolean existsByTaxId(String taxId);

    @Query("""
            SELECT fc FROM FleetCompanyEntity fc
            WHERE fc.isActive = true
            ORDER BY fc.name ASC
            """)
    List<FleetCompanyEntity> findAllActive();

    @Query("""
            SELECT fc FROM FleetCompanyEntity fc
            WHERE fc.isActive = true
            ORDER BY fc.name ASC
            """)
    Page<FleetCompanyEntity> findAllActive(Pageable pageable);

    @Query("""
            SELECT fc FROM FleetCompanyEntity fc
            WHERE fc.isActive = :isActive
            ORDER BY fc.name ASC
            """)
    Page<FleetCompanyEntity> findAllByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    @Query("""
            SELECT fc FROM FleetCompanyEntity fc
            WHERE LOWER(fc.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(fc.taxId) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(fc.corporateEmail) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY fc.name ASC
            """)
    Page<FleetCompanyEntity> searchFleets(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT COUNT(fv) FROM FleetVehicleEntity fv
            WHERE fv.companyId = :companyId
            AND fv.isActive = true
            """)
    Long countActiveVehiclesByCompanyId(@Param("companyId") Long companyId);
}
