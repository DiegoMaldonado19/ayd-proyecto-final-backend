package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.fleet.FleetVehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FleetVehicleRepository {

    /**
     * Busca vehículos de una compañía con paginación
     * 
     * @param companyId ID de la compañía de flotilla
     * @param pageable  Configuración de paginación
     * @return Página de vehículos
     */
    Page<FleetVehicle> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Busca vehículos de una compañía por estado con paginación
     * 
     * @param companyId ID de la compañía de flotilla
     * @param isActive  Estado del vehículo (true=activo, false=inactivo)
     * @param pageable  Configuración de paginación
     * @return Página de vehículos filtrados por estado
     */
    Page<FleetVehicle> findByCompanyIdAndIsActive(Long companyId, Boolean isActive, Pageable pageable);

    /**
     * Cuenta vehículos de una compañía por estado
     * 
     * @param companyId ID de la compañía de flotilla
     * @param isActive  Estado del vehículo
     * @return Cantidad de vehículos con ese estado
     */
    long countByCompanyIdAndIsActive(Long companyId, Boolean isActive);

    /**
     * Verifica si existe un vehículo con esa placa en la compañía
     * 
     * @param companyId    ID de la compañía de flotilla
     * @param licensePlate Placa del vehículo
     * @return true si existe, false si no
     */
    boolean existsByCompanyIdAndLicensePlate(Long companyId, String licensePlate);

    /**
     * Guarda o actualiza un vehículo de flotilla
     * 
     * @param fleetVehicle Vehículo a guardar
     * @return Vehículo guardado
     */
    FleetVehicle save(FleetVehicle fleetVehicle);

    /**
     * Busca un vehículo por ID
     * 
     * @param id ID del vehículo
     * @return FleetVehicle si existe
     */
    Optional<FleetVehicle> findById(Long id);
}
