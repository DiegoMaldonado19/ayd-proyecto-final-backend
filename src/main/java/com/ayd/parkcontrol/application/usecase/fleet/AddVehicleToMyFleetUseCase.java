package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.AddVehicleToFleetRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.application.mapper.FleetVehicleDtoMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.ResourceNotFoundException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.domain.model.fleet.FleetCompany;
import com.ayd.parkcontrol.domain.model.fleet.FleetVehicle;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case para que un administrador de flotilla agregue un vehículo a su
 * flotilla.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddVehicleToMyFleetUseCase {

    private final UserRepository userRepository;
    private final FleetRepository fleetRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final FleetVehicleDtoMapper fleetVehicleDtoMapper;

    /**
     * Agrega un vehículo a la flotilla del administrador autenticado.
     *
     * @param request datos del vehículo a agregar
     * @return FleetVehicleResponse con el vehículo creado
     * @throws UserNotFoundException     si el usuario autenticado no existe
     * @throws ResourceNotFoundException si la flotilla, plan o tipo de vehículo no
     *                                   existen
     * @throws BusinessRuleException     si se excede el límite de placas o la placa
     *                                   ya existe
     */
    @Transactional
    public FleetVehicleResponse execute(AddVehicleToFleetRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.debug("Adding vehicle to fleet for administrator: {}", email);

        // Obtener el usuario autenticado
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Obtener la flotilla asociada al usuario
        FleetCompany fleet = fleetRepository.findByAdminUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene una flotilla asignada"));

        // Validar que no se exceda el límite de placas
        long currentVehicleCount = fleetVehicleRepository.countByCompanyIdAndIsActive(fleet.getId(), true);
        if (currentVehicleCount >= fleet.getPlateLimit()) {
            throw new BusinessRuleException(
                    String.format("Se ha alcanzado el límite de %d vehículos para esta flotilla",
                            fleet.getPlateLimit()));
        }

        // Validar que la placa no existe en la flotilla
        if (fleetVehicleRepository.existsByCompanyIdAndLicensePlate(fleet.getId(), request.getLicensePlate())) {
            throw new BusinessRuleException(
                    "La placa " + request.getLicensePlate() + " ya existe en esta flotilla");
        }

        // Crear el vehículo
        FleetVehicle vehicle = FleetVehicle.builder()
                .companyId(fleet.getId())
                .licensePlate(request.getLicensePlate().toUpperCase())
                .driverName(request.getAssignedEmployee())
                .isActive(true)
                .build();

        FleetVehicle savedVehicle = fleetVehicleRepository.save(vehicle);

        log.info("Vehicle {} added successfully to fleet: {}", savedVehicle.getLicensePlate(), fleet.getName());

        return fleetVehicleDtoMapper.toResponse(savedVehicle);
    }
}
