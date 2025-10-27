package com.ayd.parkcontrol.infrastructure.cache;

import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de ocupación en Redis desde la base de datos.
 * 
 * Se ejecuta al iniciar la aplicación para sincronizar el estado de ocupación
 * en Redis con los tickets activos en la base de datos.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OccupancyInitializer {

    private final RedisOccupancyService redisOccupancyService;
    private final JpaBranchRepository branchRepository;
    private final JpaTicketRepository ticketRepository;

    /**
     * Sincroniza la ocupación de Redis con los tickets activos en la base de datos.
     * Se ejecuta automáticamente cuando la aplicación está lista.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void initializeOccupancy() {
        log.info("Iniciando sincronización de ocupación desde base de datos a Redis...");

        try {
            var branches = branchRepository.findAll();

            for (var branch : branches) {
                syncBranchOccupancy(branch.getId());
            }

            log.info("Sincronización de ocupación completada exitosamente para {} sucursales",
                    branches.size());
        } catch (Exception e) {
            log.error("Error durante la sincronización de ocupación", e);
        }
    }

    /**
     * Sincroniza la ocupación de una sucursal específica.
     * 
     * @param branchId ID de la sucursal
     */
    private void syncBranchOccupancy(Long branchId) {
        try {
            // Contar tickets activos (sin exit_time) por tipo de vehículo
            long occupancy2R = ticketRepository.countActiveTwoWheelerTickets(branchId);
            long occupancy4R = ticketRepository.countActiveFourWheelerTickets(branchId);

            // Establecer en Redis
            redisOccupancyService.setOccupancy(branchId, "2R", (int) occupancy2R);
            redisOccupancyService.setOccupancy(branchId, "4R", (int) occupancy4R);

            log.debug("Sincronizada ocupación para sucursal {}: 2R={}, 4R={}",
                    branchId, occupancy2R, occupancy4R);
        } catch (Exception e) {
            log.error("Error sincronizando ocupación para sucursal {}", branchId, e);
        }
    }
}
