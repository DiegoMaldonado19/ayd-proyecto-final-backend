package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.application.mapper.FleetVehicleDtoMapper;
import com.ayd.parkcontrol.domain.exception.ResourceNotFoundException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.domain.model.fleet.FleetCompany;
import com.ayd.parkcontrol.domain.model.fleet.FleetVehicle;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.FleetRepository;
import com.ayd.parkcontrol.domain.repository.FleetVehicleRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case para listar los vehículos de la flotilla del administrador
 * autenticado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListMyFleetVehiclesUseCase {

    private final UserRepository userRepository;
    private final FleetRepository fleetRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final FleetVehicleDtoMapper fleetVehicleDtoMapper;

    /**
     * Lista los vehículos de la flotilla del administrador autenticado.
     *
     * @param pageable información de paginación
     * @return Page con los vehículos de la flotilla
     * @throws UserNotFoundException     si el usuario autenticado no existe
     * @throws ResourceNotFoundException si el usuario no tiene una flotilla
     *                                   asignada
     */
    @Transactional(readOnly = true)
    public Page<FleetVehicleResponse> execute(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.debug("Listing vehicles for fleet administrator: {}", email);

        // Obtener el usuario autenticado
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Obtener la flotilla asociada al usuario
        FleetCompany fleet = fleetRepository.findByAdminUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene una flotilla asignada"));

        // Obtener los vehículos de la flotilla
        Page<FleetVehicle> vehicles = fleetVehicleRepository.findByCompanyId(fleet.getId(), pageable);

        log.info("Found {} vehicles for fleet: {}", vehicles.getTotalElements(), fleet.getName());

        return vehicles.map(fleetVehicleDtoMapper::toResponse);
    }

    /**
     * Lista solo los vehículos activos de la flotilla del administrador
     * autenticado.
     *
     * @param pageable información de paginación
     * @return Page con los vehículos activos
     */
    @Transactional(readOnly = true)
    public Page<FleetVehicleResponse> executeActive(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.debug("Listing active vehicles for fleet administrator: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        FleetCompany fleet = fleetRepository.findByAdminUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene una flotilla asignada"));

        Page<FleetVehicle> vehicles = fleetVehicleRepository.findByCompanyIdAndIsActive(
                fleet.getId(), true, pageable);

        log.info("Found {} active vehicles for fleet: {}", vehicles.getTotalElements(), fleet.getName());

        return vehicles.map(fleetVehicleDtoMapper::toResponse);
    }
}
