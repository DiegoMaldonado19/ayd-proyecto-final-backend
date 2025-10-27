package com.ayd.parkcontrol.infrastructure.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Servicio de limitación de tasa (rate limiting) usando Redis.
 * 
 * Regla de Negocio: Protege endpoints críticos contra ataques de fuerza bruta
 * y abuso mediante la limitación de intentos por IP en ventanas de tiempo.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Verifica si el límite de tasa ha sido excedido para un endpoint + IP.
     * 
     * @param endpoint      ruta del endpoint (ej: "/api/v1/auth/login")
     * @param ip            dirección IP del cliente
     * @param maxAttempts   número máximo de intentos permitidos
     * @param windowSeconds ventana de tiempo en segundos
     * @return true si el límite fue excedido, false si aún puede hacer requests
     */
    public boolean isRateLimitExceeded(String endpoint, String ip, int maxAttempts, int windowSeconds) {
        String key = buildKey(endpoint, ip);

        // Obtener el valor actual
        String currentValue = redisTemplate.opsForValue().get(key);

        if (currentValue == null) {
            // Primera petición: inicializar contador con TTL
            redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(windowSeconds));
            log.debug("Primera petición para endpoint={}, ip={}. Contador: 1/{}", endpoint, ip, maxAttempts);
            return false;
        }

        try {
            int attempts = Integer.parseInt(currentValue);

            if (attempts >= maxAttempts) {
                log.warn("Límite de tasa excedido para endpoint={}, ip={}. Intentos: {}/{}",
                        endpoint, ip, attempts, maxAttempts);
                return true;
            }

            // Incrementar contador (sin modificar TTL)
            Long newAttempts = redisTemplate.opsForValue().increment(key);
            log.debug("Petición registrada para endpoint={}, ip={}. Contador: {}/{}",
                    endpoint, ip, newAttempts, maxAttempts);

            return false;
        } catch (NumberFormatException e) {
            log.error("Valor inválido en Redis para key: {}. Valor: {}. Reiniciando.", key, currentValue);
            redisTemplate.delete(key);
            return false;
        }
    }

    /**
     * Obtiene el tiempo restante (en segundos) hasta que se libere el bloqueo.
     * 
     * @param endpoint ruta del endpoint
     * @param ip       dirección IP del cliente
     * @return segundos restantes hasta que expire el bloqueo (0 si no está
     *         bloqueado)
     */
    public long getRemainingLockTime(String endpoint, String ip) {
        String key = buildKey(endpoint, ip);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * Resetea manualmente el contador de intentos para un endpoint + IP.
     * Útil para desbloquear a un usuario legítimo.
     * 
     * @param endpoint ruta del endpoint
     * @param ip       dirección IP del cliente
     */
    public void resetLimit(String endpoint, String ip) {
        String key = buildKey(endpoint, ip);
        redisTemplate.delete(key);
        log.info("Límite de tasa reseteado para endpoint={}, ip={}", endpoint, ip);
    }

    /**
     * Construye la clave de Redis para rate limiting.
     * 
     * @param endpoint ruta del endpoint
     * @param ip       dirección IP
     * @return clave de Redis
     */
    private String buildKey(String endpoint, String ip) {
        return RATE_LIMIT_KEY_PREFIX + endpoint + ":" + ip;
    }
}
