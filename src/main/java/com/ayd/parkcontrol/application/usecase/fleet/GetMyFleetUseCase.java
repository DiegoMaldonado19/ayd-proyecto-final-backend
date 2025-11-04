package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.domain.exception.ResourceNotFoundException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.domain.model.fleet.FleetCompany;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.FleetRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case para obtener la flotilla del administrador de flotilla autenticado.
 * El administrador solo puede ver su propia flotilla asignada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetMyFleetUseCase {

    private final UserRepository userRepository;
    private final FleetRepository fleetRepository;
    private final FleetDtoMapper fleetDtoMapper;

    /**
     * Obtiene la flotilla asociada al administrador de flotilla autenticado.
     *
     * @return FleetResponse con los datos de la flotilla
     * @throws UserNotFoundException     si el usuario autenticado no existe
     * @throws ResourceNotFoundException si el usuario no tiene una flotilla
     *                                   asignada
     */
    @Transactional(readOnly = true)
    public FleetResponse execute() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.debug("Getting fleet for fleet administrator: {}", email);

        // Obtener el usuario autenticado
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Obtener la flotilla asociada al usuario
        // NOTA: Esto requiere que exista una relación user_id en fleet_companies
        // o una tabla intermedia fleet_administrators
        FleetCompany fleet = fleetRepository.findByAdminUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene una flotilla asignada. Contacte al administrador del sistema."));

        log.info("Fleet retrieved successfully for user: {}, fleet: {}", email, fleet.getName());

        return fleetDtoMapper.toResponse(fleet);
    }
}
