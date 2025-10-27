package com.ayd.parkcontrol.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestión de ocupación en tiempo real usando Redis.
 * 
 * Regla de Negocio: Control de concurrencia atómico para evitar sobreventa
 * de espacios de estacionamiento. Usa operaciones INCR/DECR de Redis que
 * son atómicas y thread-safe.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisOccupancyService {

    private static final String OCCUPANCY_KEY_PREFIX = "branch:occupancy:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Intenta reservar un espacio de estacionamiento de forma atómica.
     * 
     * Si la nueva ocupación excede la capacidad, realiza un rollback automático
     * decrementando el contador.
     * 
     * @param branchId    ID de la sucursal
     * @param vehicleType tipo de vehículo (2R o 4R)
     * @param capacity    capacidad máxima para este tipo de vehículo
     * @return true si se reservó exitosamente, false si no hay espacios disponibles
     */
    public boolean tryReserveSpace(Long branchId, String vehicleType, int capacity) {
        String key = buildKey(branchId, vehicleType);

        log.debug("Intentando reservar espacio en sucursal {} para tipo {}", branchId, vehicleType);

        // Operación atómica de incremento
        Long newOccupancy = redisTemplate.opsForValue().increment(key);

        if (newOccupancy == null) {
            log.error("Error al incrementar ocupación en Redis para key: {}", key);
            return false;
        }

        // Verificar si excedió la capacidad
        if (newOccupancy > capacity) {
            // Rollback: decrementar para deshacer la reserva
            redisTemplate.opsForValue().decrement(key);
            log.warn("Capacidad excedida en sucursal {} para tipo {}. Ocupación: {}, Capacidad: {}",
                    branchId, vehicleType, newOccupancy - 1, capacity);
            return false;
        }

        log.info("Espacio reservado exitosamente. Sucursal: {}, Tipo: {}, Nueva ocupación: {}/{}",
                branchId, vehicleType, newOccupancy, capacity);
        return true;
    }

    /**
     * Libera un espacio de estacionamiento decrementando el contador.
     * 
     * @param branchId    ID de la sucursal
     * @param vehicleType tipo de vehículo (2R o 4R)
     */
    public void releaseSpace(Long branchId, String vehicleType) {
        String key = buildKey(branchId, vehicleType);

        log.debug("Liberando espacio en sucursal {} para tipo {}", branchId, vehicleType);

        // Operación atómica de decremento
        Long newOccupancy = redisTemplate.opsForValue().decrement(key);

        if (newOccupancy == null) {
            log.error("Error al decrementar ocupación en Redis para key: {}", key);
            return;
        }

        // Validar que no sea negativo (por si acaso)
        if (newOccupancy < 0) {
            log.warn("Ocupación negativa detectada en key: {}. Ajustando a 0", key);
            redisTemplate.opsForValue().set(key, "0");
        }

        log.info("Espacio liberado. Sucursal: {}, Tipo: {}, Nueva ocupación: {}",
                branchId, vehicleType, Math.max(newOccupancy, 0));
    }

    /**
     * Obtiene la ocupación actual de una sucursal para un tipo de vehículo.
     * 
     * @param branchId    ID de la sucursal
     * @param vehicleType tipo de vehículo (2R o 4R)
     * @return ocupación actual (0 si no existe)
     */
    public int getCurrentOccupancy(Long branchId, String vehicleType) {
        String key = buildKey(branchId, vehicleType);
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return 0;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("Valor inválido en Redis para key: {}. Valor: {}", key, value);
            return 0;
        }
    }

    /**
     * Establece la ocupación actual de una sucursal.
     * Usado para sincronización inicial desde la base de datos.
     * 
     * @param branchId    ID de la sucursal
     * @param vehicleType tipo de vehículo (2R o 4R)
     * @param occupancy   ocupación a establecer
     */
    public void setOccupancy(Long branchId, String vehicleType, int occupancy) {
        String key = buildKey(branchId, vehicleType);

        if (occupancy < 0) {
            log.warn("Intentando establecer ocupación negativa. Ajustando a 0");
            occupancy = 0;
        }

        redisTemplate.opsForValue().set(key, String.valueOf(occupancy));
        log.debug("Ocupación establecida. Key: {}, Valor: {}", key, occupancy);
    }

    /**
     * Resetea la ocupación de una sucursal a 0.
     * 
     * @param branchId    ID de la sucursal
     * @param vehicleType tipo de vehículo (2R o 4R)
     */
    public void resetOccupancy(Long branchId, String vehicleType) {
        setOccupancy(branchId, vehicleType, 0);
        log.info("Ocupación reseteada para sucursal {} tipo {}", branchId, vehicleType);
    }

    /**
     * Construye la clave de Redis para ocupación.
     * 
     * @param branchId    ID de la sucursal
     * @param vehicleType tipo de vehículo
     * @return clave de Redis
     */
    private String buildKey(Long branchId, String vehicleType) {
        return OCCUPANCY_KEY_PREFIX + branchId + ":" + vehicleType;
    }
}
