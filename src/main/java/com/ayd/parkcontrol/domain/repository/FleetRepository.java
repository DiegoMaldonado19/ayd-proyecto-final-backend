package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.fleet.FleetCompany;

import java.math.BigDecimal;
import java.util.Optional;

public interface FleetRepository {

    /**
     * Busca la compañía de flotilla asociada a un usuario administrador
     * 
     * @param userId ID del usuario administrador de flotilla
     * @return FleetCompany si existe, Optional.empty() si no
     */
    Optional<FleetCompany> findByAdminUserId(Long userId);

    /**
     * Obtiene el consumo total histórico de una flotilla
     * 
     * @param fleetId ID de la compañía de flotilla
     * @return Monto total consumido
     */
    Optional<BigDecimal> getTotalConsumptionByFleetId(Long fleetId);

    /**
     * Obtiene el consumo del mes actual de una flotilla
     * 
     * @param fleetId ID de la compañía de flotilla
     * @return Monto consumido en el mes actual
     */
    Optional<BigDecimal> getCurrentMonthConsumptionByFleetId(Long fleetId);

    /**
     * Cuenta vehículos activos de una flotilla
     * 
     * @param fleetId ID de la compañía de flotilla
     * @return Cantidad de vehículos activos
     */
    Integer countActiveVehiclesByFleetId(Long fleetId);

    /**
     * Cuenta total de vehículos de una flotilla (activos e inactivos)
     * 
     * @param fleetId ID de la compañía de flotilla
     * @return Cantidad total de vehículos
     */
    Integer countTotalVehiclesByFleetId(Long fleetId);

    /**
     * Guarda o actualiza una compañía de flotilla
     * 
     * @param fleetCompany Compañía a guardar
     * @return Compañía guardada
     */
    FleetCompany save(FleetCompany fleetCompany);

    /**
     * Busca una compañía de flotilla por ID
     * 
     * @param id ID de la compañía
     * @return FleetCompany si existe
     */
    Optional<FleetCompany> findById(Long id);
}
