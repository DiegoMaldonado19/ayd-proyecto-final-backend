package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetConsumptionResponse;
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

import java.math.BigDecimal;

/**
 * Use case para obtener estadísticas de consumo de la flotilla del
 * administrador autenticado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetMyFleetConsumptionUseCase {

    private final UserRepository userRepository;
    private final FleetRepository fleetRepository;

    /**
     * Obtiene las estadísticas de consumo de la flotilla del administrador
     * autenticado.
     *
     * @return FleetConsumptionResponse con las estadísticas
     * @throws UserNotFoundException     si el usuario autenticado no existe
     * @throws ResourceNotFoundException si el usuario no tiene una flotilla
     *                                   asignada
     */
    @Transactional(readOnly = true)
    public FleetConsumptionResponse execute() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.debug("Getting consumption for fleet administrator: {}", email);

        // Obtener el usuario autenticado
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Obtener la flotilla asociada al usuario
        FleetCompany fleet = fleetRepository.findByAdminUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene una flotilla asignada"));

        // Obtener estadísticas de consumo
        // NOTA: Estas consultas deben implementarse en FleetRepository
        BigDecimal totalConsumption = fleetRepository.getTotalConsumptionByFleetId(fleet.getId())
                .orElse(BigDecimal.ZERO);

        Integer totalVehicles = fleetRepository.countTotalVehiclesByFleetId(fleet.getId());

        log.info("Consumption stats retrieved for fleet: {}, total: {}",
                fleet.getName(), totalConsumption);

        return FleetConsumptionResponse.builder()
                .companyId(fleet.getId())
                .companyName(fleet.getName())
                .totalVehicles(totalVehicles)
                .totalAmountCharged(totalConsumption)
                .build();
    }
}
