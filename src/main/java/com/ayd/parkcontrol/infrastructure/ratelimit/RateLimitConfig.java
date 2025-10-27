package com.ayd.parkcontrol.infrastructure.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Configuración de rate limit para un endpoint específico.
 */
@Getter
@AllArgsConstructor
public class RateLimitConfig {

    private final int maxAttempts;
    private final int windowSeconds;
}
