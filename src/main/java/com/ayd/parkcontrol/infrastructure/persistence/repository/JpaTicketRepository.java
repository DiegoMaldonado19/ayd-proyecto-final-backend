package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaTicketRepository extends JpaRepository<TicketEntity, Long> {

    /**
     * Buscar ticket por folio y sucursal
     */
    Optional<TicketEntity> findByBranchIdAndFolio(Long branchId, String folio);

    /**
     * Buscar ticket por folio (sin filtrar por sucursal)
     */
    Optional<TicketEntity> findByFolio(String folio);

    /**
     * Buscar tickets por placa
     */
    List<TicketEntity> findByLicensePlateOrderByEntryTimeDesc(String licensePlate);

    /**
     * Buscar tickets activos (en progreso)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.statusTypeId = :statusTypeId ORDER BY t.entryTime DESC")
    List<TicketEntity> findByStatusTypeId(@Param("statusTypeId") Integer statusTypeId);

    /**
     * Buscar tickets activos por sucursal
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.branchId = :branchId ORDER BY t.entryTime DESC")
    List<TicketEntity> findByBranchIdOrderByEntryTimeDesc(@Param("branchId") Long branchId);

    /**
     * Buscar tickets activos por sucursal y estado
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.branchId = :branchId AND t.statusTypeId = :statusTypeId ORDER BY t.entryTime DESC")
    List<TicketEntity> findByBranchIdAndStatusTypeId(@Param("branchId") Long branchId,
            @Param("statusTypeId") Integer statusTypeId);

    /**
     * Contar tickets activos por sucursal y tipo de vehículo
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.branchId = :branchId AND t.vehicleTypeId = :vehicleTypeId AND t.statusTypeId = :statusTypeId")
    Long countActiveTicketsByBranchAndVehicleType(@Param("branchId") Long branchId,
            @Param("vehicleTypeId") Integer vehicleTypeId, @Param("statusTypeId") Integer statusTypeId);

    /**
     * Verificar si existe un ticket activo para una placa en una sucursal
     */
    @Query("SELECT COUNT(t) > 0 FROM TicketEntity t WHERE t.licensePlate = :licensePlate AND t.branchId = :branchId AND t.statusTypeId = :statusTypeId")
    Boolean existsActiveTicketForPlateInBranch(@Param("licensePlate") String licensePlate,
            @Param("branchId") Long branchId, @Param("statusTypeId") Integer statusTypeId);
}
