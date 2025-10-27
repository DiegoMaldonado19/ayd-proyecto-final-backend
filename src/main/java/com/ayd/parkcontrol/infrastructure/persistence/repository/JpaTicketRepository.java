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

        /**
         * Obtener placas de vehículos de una flotilla (basado en fleet_vehicles)
         */
        @Query(value = "SELECT DISTINCT fv.license_plate FROM fleet_vehicles fv WHERE fv.company_id = :companyId AND fv.is_active = true", nativeQuery = true)
        List<String> findLicensePlatesByFleetCompany(@Param("companyId") Long companyId);

        /**
         * Contar entradas por lista de placas
         */
        @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.licensePlate IN :licensePlates")
        Long countEntriesByLicensePlates(@Param("licensePlates") List<String> licensePlates);

        /**
         * Sumar horas consumidas por lista de placas
         */
        @Query(value = "SELECT COALESCE(SUM(tc.total_hours), 0) FROM tickets t " +
                        "INNER JOIN ticket_charges tc ON t.id = tc.ticket_id " +
                        "WHERE t.license_plate IN :licensePlates", nativeQuery = true)
        java.math.BigDecimal sumHoursByLicensePlates(@Param("licensePlates") List<String> licensePlates);

        /**
         * Sumar monto cobrado por lista de placas
         */
        @Query(value = "SELECT COALESCE(SUM(tc.total_amount), 0) FROM tickets t " +
                        "INNER JOIN ticket_charges tc ON t.id = tc.ticket_id " +
                        "WHERE t.license_plate IN :licensePlates", nativeQuery = true)
        java.math.BigDecimal sumAmountByLicensePlates(@Param("licensePlates") List<String> licensePlates);

        /**
         * Contar entradas por placa específica
         */
        @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.licensePlate = :licensePlate")
        Long countEntriesByLicensePlate(@Param("licensePlate") String licensePlate);

        /**
         * Sumar horas consumidas por placa específica
         */
        @Query(value = "SELECT COALESCE(SUM(tc.total_hours), 0) FROM tickets t " +
                        "INNER JOIN ticket_charges tc ON t.id = tc.ticket_id " +
                        "WHERE t.license_plate = :licensePlate", nativeQuery = true)
        java.math.BigDecimal sumHoursByLicensePlate(@Param("licensePlate") String licensePlate);

        /**
         * Sumar monto cobrado por placa específica
         */
        @Query(value = "SELECT COALESCE(SUM(tc.total_amount), 0) FROM tickets t " +
                        "INNER JOIN ticket_charges tc ON t.id = tc.ticket_id " +
                        "WHERE t.license_plate = :licensePlate", nativeQuery = true)
        java.math.BigDecimal sumAmountByLicensePlate(@Param("licensePlate") String licensePlate);

        /**
         * Obtener última entrada por placa
         */
        @Query("SELECT MAX(t.entryTime) FROM TicketEntity t WHERE t.licensePlate = :licensePlate")
        java.time.LocalDateTime findLastEntryByLicensePlate(@Param("licensePlate") String licensePlate);

        /**
         * Contar tickets activos (sin exit_time) de dos ruedas en una sucursal.
         * Usado para sincronización de ocupación con Redis.
         */
        @Query(value = """
                            SELECT COUNT(t.id)
                            FROM tickets t
                            INNER JOIN vehicle_types vt ON t.vehicle_type_id = vt.id
                            WHERE t.branch_id = :branchId
                              AND vt.code = '2R'
                              AND t.exit_time IS NULL
                        """, nativeQuery = true)
        long countActiveTwoWheelerTickets(@Param("branchId") Long branchId);

        /**
         * Contar tickets activos (sin exit_time) de cuatro ruedas en una sucursal.
         * Usado para sincronización de ocupación con Redis.
         */
        @Query(value = """
                            SELECT COUNT(t.id)
                            FROM tickets t
                            INNER JOIN vehicle_types vt ON t.vehicle_type_id = vt.id
                            WHERE t.branch_id = :branchId
                              AND vt.code = '4R'
                              AND t.exit_time IS NULL
                        """, nativeQuery = true)
        long countActiveFourWheelerTickets(@Param("branchId") Long branchId);
}
